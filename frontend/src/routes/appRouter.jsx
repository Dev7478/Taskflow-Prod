import { createBrowserRouter, Navigate } from "react-router-dom";
import AppLayout from "../layout/AppLayout";
import ErrorPage from "../pages/ErrorPage";
import ProtectedRoute from "../components/ProtectedRoute";
import RoleProtectedRoute from "../components/RoleProtectedRoute";
import { Suspense, lazy } from "react";
import Login from "../pages/Login.jsx";
import OTPRequest from "../pages/OTPRequest.jsx";
import UpdatePassword from "../pages/UpdatePassword.jsx";
import RegisterUser from "../pages/RegisterUser.jsx";

// Lazy load pages
const Dashboard = lazy(() => import("../pages/Dashboard.jsx"));
const TicketComment = lazy(() => import("../pages/TicketComponent.jsx"));

export const appRouter = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        element: <Navigate to="login" replace />,
      },

      // relative child path (NOT "/login")
      {
        path: "login",
        element: <Login />,
      },

      {
        path: "dashboard",
        element: (
          <ProtectedRoute>
            <RoleProtectedRoute allowedRoles={["ROLE_ADMIN", "ROLE_USER"]}>
              <Suspense fallback={<p>Loading...</p>}>
                <Dashboard />
              </Suspense>
            </RoleProtectedRoute>
          </ProtectedRoute>
        ),
      },

      {
        path: "tickets/:id/comments",
        element: (
          <ProtectedRoute>
            <RoleProtectedRoute allowedRoles={["ROLE_ADMIN", "ROLE_USER"]}>
              <Suspense fallback={<p>Loading...</p>}>
                <TicketComment />
              </Suspense>
            </RoleProtectedRoute>
          </ProtectedRoute>
        ),
      },

      {
        path: "otp_request",
        element: <OTPRequest />,
      },

      {
        path: "update_password",
        element: <UpdatePassword />,
      },

      {
        path: "register",
        element: <RegisterUser />,
      },
    ],
  },
]);
