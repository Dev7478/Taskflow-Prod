import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";

const UpdatePassword = ({ otpLength = 6 }) => {
  const [otpFields, setOtpFields] = useState(new Array(otpLength).fill(""));
  const ref = useRef([]);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const navigate = useNavigate();

  function handleKeyDown(e, index) {
    const key = e.key;
    if (key === "ArrowLeft") {
      if (index > 0) ref.current[index - 1].focus();
      return;
    }
    if (key === "ArrowRight") {
      if (index + 1 < otpFields.length) ref.current[index + 1].focus();
      return;
    }
    const copyOtpFields = [...otpFields];
    if (key === "Backspace") {
      copyOtpFields[index] = "";
      setOtpFields(copyOtpFields);

      if (index > 0) ref.current[index - 1].focus();
      return;
    }
    if (!/^\d$/.test(key)) {
      return;
    }
    copyOtpFields[index] = key;
    setOtpFields(copyOtpFields);
    if (index + 1 < otpFields.length) ref.current[index + 1].focus();
  }

  function handlePaste(e) {
    e.preventDefault();
    const pastedData = e.clipboardData.getData("text");
    const digits = pastedData.match(/\d/g);
    if (!digits) return;
    const nextOtp = otpFields.slice();
    for (let i = 0; i < otpLength && digits.length; ++i) {
      nextOtp[i] = digits[i];
    }
    setOtpFields(nextOtp);
    const nextFocus = digits.length < otpLength ? digits.length : otpLength - 1;
    ref.current[nextFocus]?.focus();
  }

  useEffect(() => {
    ref.current[0]?.focus();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    const otp = otpFields.join("");
    if (!email) {
      setError("Please enter your registered email.");
      return;
    }
    if (!otp || otp.length !== otpLength) {
      setError("Please enter the full OTP.");
      return;
    }
    if (!password) {
      setError("Please enter a new password.");
      return;
    }
    if (password !== confirmPassword) {
      setError("Password and confirm password do not match.");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch("http://localhost:8082/api/users", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password, otp }),
      });

      const data = await res.json().catch(() => ({}));

      if (res.ok) {
        setSuccess("Password updated successfully. Redirecting to login...");
        setTimeout(() => navigate("/login"), 1200);
      } else {
        setError(data.message || "Failed to update password. Please try again.");
      }
    } catch (err) {
      setError("Network error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-6">
      <div className="w-full max-w-2xl">
        {/* OTP inputs */}
        <div className="flex justify-center mb-6">
          {otpFields.map((value, index) => (
            <input
              key={index}
              ref={(currentInput) => (ref.current[index] = currentInput)}
              value={value}
              type="text"
              maxLength={1}
              className="w-12 h-12 mx-1 text-center text-xl font-semibold border-2 border-blue-400 rounded-lg focus:outline-none focus:border-blue-600 bg-blue-50 transition"
              onKeyDown={(e) => handleKeyDown(e, index)}
              onPaste={handlePaste}
              onChange={() => {}}
            />
          ))}
        </div>

        {/* Card with email/password fields */}
        <form
          onSubmit={handleSubmit}
          className="mx-auto w-96 bg-white rounded-2xl shadow-md p-6"
          aria-label="Update password"
        >
          <h2 className="text-2xl font-semibold text-center text-blue-600 mb-4">
            Set New Password
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
            className="w-full p-2 mb-3 border rounded focus:outline-none focus:ring-2 focus:ring-blue-300"
            placeholder="you@example.com"
          />

          <label className="block text-sm text-gray-700 mb-1" htmlFor="password">
            New Password
          </label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="w-full p-2 mb-3 border rounded focus:outline-none focus:ring-2 focus:ring-blue-300"
            placeholder="New password"
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
            className="w-full p-2 mb-4 border rounded focus:outline-none focus:ring-2 focus:ring-blue-300"
            placeholder="Confirm new password"
          />

          <button
            type="submit"
            disabled={loading}
            className={`w-full py-2 rounded text-white transition ${loading ? "bg-blue-300 cursor-not-allowed" : "bg-blue-600 hover:bg-blue-700"}`}
          >
            {loading ? "Updating..." : "Update Password"}
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
    </div>
  );
};

export default UpdatePassword;