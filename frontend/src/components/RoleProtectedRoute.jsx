import { Navigate } from "react-router-dom";
import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

const RoleProtectedRoute = ({ allowedRoles, children }) => {
  const { token, user } = useContext(AuthContext);

  if (!token) return <Navigate to="/" replace />;
  if (allowedRoles && !allowedRoles.includes(user?.role))
    return <Navigate to="/unauthorized" replace />;

  return children;
};

export default RoleProtectedRoute;