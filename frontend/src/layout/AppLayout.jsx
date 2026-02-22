import { Outlet } from "react-router-dom";
import Header from "../components/Header";
import Footer from "../components/Footer";

const AppLayout = () => {
  return (
    <div className="app bg-gray-100 min-h-screen">
      <Header />
      <main className="p-6">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
};

export default AppLayout;