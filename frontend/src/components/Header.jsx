import { Link, useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import { useContext } from "react";
import logo from "../assets/taskflow_logo.png";

const Header = () => {
  const { logout, user, isAuthenticated, loading } = useContext(AuthContext);
  const navigate = useNavigate();

  // consider authenticated only if isAuthenticated === true or user is a non-empty object
  const isLoggedIn = Boolean(
    isAuthenticated === true || (user && typeof user === "object" && Object.keys(user).length > 0)
  );

  return (
    <header className="bg-blue-600 text-white p-4 flex justify-between items-center">
      {/* <h1 className="text-xl font-bold">TaskFlow V3</h1> */}

      <Link to="/" className="flex items-center gap-3">
        <img
          src={logo}
          className="w-8 h-8 text-white"
          alt="logo"
        />
        <span className="text-xl font-bold">TaskFlow V3</span>
      </Link>

      <nav className="space-x-4 flex items-center">
        <Link to="/dashboard" className="hover:underline">
          Dashboard
        </Link>

        <button
          onClick={() => {
            if (!isLoggedIn) return;
            logout();
            navigate("/");
          }}
          disabled={!isLoggedIn || loading}
          className={`px-4 py-2 rounded transition ${isLoggedIn
              ? "bg-red-500 hover:bg-red-600 text-white"
              : "bg-red-500/60 text-white opacity-60 cursor-not-allowed"
            }`}
        >
          Logout
        </button>
      </nav>
    </header>
  );
};

export default Header;