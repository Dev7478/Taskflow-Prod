import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const OTPRequest = () => {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    if (!email) {
      setError("Please enter your registered email.");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch("http://localhost:8082/api/otps", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });

      const data = await res.json().catch(() => ({}));

      if (res.ok) {
        setSuccess("OTP sent to your email.");
        // navigate to OTP verify page if you have one (adjust path as needed)
        setTimeout(() => navigate("/update_password"), 900);
      } else {
        setError(data.message || "Failed to send OTP. Please try again.");
      }
    } catch (err) {
      setError("Network error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <form
        onSubmit={handleSubmit}
        className="w-96 bg-white rounded-2xl shadow-md p-6"
        aria-label="Request OTP"
      >
        <h2 className="text-2xl font-semibold text-center text-blue-600 mb-4">
          Forgot Password
        </h2>

        {error && <p className="text-sm text-red-500 mb-3">{error}</p>}
        {success && <p className="text-sm text-green-600 mb-3">{success}</p>}

        <label className="block text-sm text-gray-700 mb-1" htmlFor="email">
          Registered Email
        </label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          className="w-full p-2 mb-4 border rounded focus:outline-none focus:ring-2 focus:ring-blue-300"
          placeholder="you@example.com"
        />

        <button
          type="submit"
          disabled={loading}
          className={`w-full py-2 rounded text-white transition ${
            loading ? "bg-blue-300 cursor-not-allowed" : "bg-blue-600 hover:bg-blue-700"
          }`}
        >
          {loading ? "Sending..." : "Send OTP"}
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

export default OTPRequest;