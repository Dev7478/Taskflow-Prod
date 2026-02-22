import { useState } from "react";
import { useNavigate } from "react-router-dom";

const RegisterUser = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [role, setRole] = useState("ROLE_USER");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!email) return setError("Email is required.");
    if (!password) return setError("Password is required.");
    if (password !== confirmPassword)
      return setError("Passwords do not match.");

    setLoading(true);
    try {
      const res = await fetch("http://localhost:8082/api/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password, role }),
      });

      const data = await res.json().catch(() => ({}));

      if (res.ok && res.status === 201) {
        const message = data.message || "User registered successfully.";
        setSuccess(message);
        // popup
        alert(message);
        // flush form
        setEmail("");
        setPassword("");
        setConfirmPassword("");
        setRole("ROLE_USER");
        // redirect to login
        navigate("/login");
      } else {
        // handle known failure (e.g., user exists -> 409) or other errors
        const errMsg =
          data.message ||
          (res.status === 409
            ? "User already exists in the database."
            : "Registration failed. Please try again.");
        setError(errMsg);
        // optional popup for failure
        alert(errMsg);
      }
    } catch (err) {
      const errMsg = "Network error. Please try again.";
      setError(errMsg);
      alert(errMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-6">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-md bg-white rounded-2xl shadow-md p-6"
        aria-label="Register user"
      >
        <h2 className="text-2xl font-semibold text-center text-blue-600 mb-4">
          Register
        </h2>

        {error && <p className="text-sm text-red-500 mb-3">{error}</p>}
        {success && <p className="text-sm text-green-600 mb-3">{success}</p>}

        <label className="block text-sm text-gray-700 mb-1" htmlFor="email">
          Email
        </label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          className="w-full p-2 mb-3 border rounded focus:outline-none focus:ring-2 focus:ring-blue-300"
          placeholder="you@example.com"
        />

        <label className="block text-sm text-gray-700 mb-1" htmlFor="password">
          Password
        </label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          className="w-full p-2 mb-3 border rounded focus:outline-none focus:ring-2 focus:ring-blue-300"
          placeholder="Enter password"
        />

        <label
          className="block text-sm text-gray-700 mb-1"
          htmlFor="confirmPassword"
        >
          Confirm Password
        </label>
        <input
          id="confirmPassword"
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
          className="w-full p-2 mb-3 border rounded focus:outline-none focus:ring-2 focus:ring-blue-300"
          placeholder="Confirm password"
        />

        <label className="block text-sm text-gray-700 mb-1" htmlFor="role">
          User Role
        </label>
        <select
          id="role"
          value={role}
          onChange={(e) => setRole(e.target.value)}
          className="w-full p-2 mb-4 border rounded focus:outline-none focus:ring-2 focus:ring-blue-300"
        >
          <option value="ROLE_ADMIN">ADMIN</option>
          <option value="ROLE_USER">USER</option>
        </select>

        <button
          type="submit"
          disabled={loading}
          className={`w-full py-2 rounded text-white transition ${
            loading
              ? "bg-blue-300 cursor-not-allowed"
              : "bg-blue-600 hover:bg-blue-700"
          }`}
        >
          {loading ? "Registering..." : "Register"}
        </button>

        <div className="mt-4 text-center text-sm text-gray-600">
          <button
            type="button"
            onClick={() => navigate("/login")}
            className="text-blue-600 hover:underline"
          >
            Back to login
          </button>
        </div>
      </form>
    </div>
  );
};

export default RegisterUser;
