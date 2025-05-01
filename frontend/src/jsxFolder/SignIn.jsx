import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import logo from "./logo.png";
import { Eye, EyeOff } from "lucide-react"; 

const SignIn = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false); 
  const [errorMessage, setErrorMessage] = useState("");

  const navigate = useNavigate();

  const handleLogin = async () => {
    if (!username || !password) {
      setErrorMessage("Please enter both username and password.");
      return;
    }

    try {
      const res = await fetch("http://localhost:8080/api/auth/signin", {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ name: username, password }),
      });

      if (res.ok) {
        const parsedData = await res.json();
        localStorage.setItem(
          "teacher",
          JSON.stringify({
            id: parsedData.id,
            name: parsedData.name,
            email: parsedData.email,
            accessToken: parsedData.accessToken,
            tokenType: parsedData.tokenType,
          })
        );
        navigate("/dashboard");
      } else {
        const errorText = await res.text();
        setErrorMessage(errorText || "Invalid username or password.");
      }
    } catch (error) {
      console.error("Login error:", error);
      setErrorMessage("Something went wrong. Please try again.");
    }
  };

  const handleForgotPassword = () => {
    // navigate("/ForgotPassword"); 
  };

  return (
    <div className="d-flex justify-content-center align-items-center min-vh-100 bg-gradient" style={{ background: "linear-gradient(135deg, #2A4B70, #6C82A8)" }}>
      <motion.div
        className="p-5 rounded-4 shadow-lg bg-white"
        style={{ width: "100%", maxWidth: "420px" }}
        initial={{ opacity: 0, y: -30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <div className="text-center mb-4">
          <img
            src={logo}
            alt="Logo"
            style={{ width: "100px", height: "100px", objectFit: "contain" }}
          />
        </div>

        <h3 className="text-center mb-4 fw-bold" style={{ color: "#2A4B70" }}>
          Welcome Back
        </h3>

        <div className="form-group mb-3">
          <label htmlFor="username" className="form-label text-muted">
            Username
          </label>
          <input
            type="text"
            id="username"
            className="form-control p-3 rounded-3"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Enter your username"
            style={{ backgroundColor: "#f8f9fa" }}
          />
        </div>

        <div className="form-group mb-4 position-relative">
          <label htmlFor="password" className="form-label text-muted">
            Password
          </label>
          <input
            type={showPassword ? "text" : "password"} 
            id="password"
            className="form-control p-3 rounded-3"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter your password"
            style={{ backgroundColor: "#f8f9fa" }}
          />
          {/* زر العين */}
          <span
            onClick={() => setShowPassword(!showPassword)}
            style={{
              position: "absolute",
              top: "38px",
              right: "20px",
              cursor: "pointer",
              color: "#6c757d"
            }}
          >
            {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
          </span>
        </div>

        <button
          onClick={handleLogin}
          className="btn w-100 py-3 rounded-3 fw-bold"
          style={{ backgroundColor: "#2A4B70", color: "#fff", fontSize: "18px" }}
        >
          Sign In
        </button>

        <div className="text-center mt-3">
          <button
            onClick={handleForgotPassword}
            className="btn btn-link"
            style={{ fontSize: "14px", textDecoration: "underline", color: "#2A4B70" }}
          >
            Forgot Password?
          </button>
        </div>

        {errorMessage && (
          <div className="alert alert-danger text-center mt-3 p-2 rounded-3" style={{ fontSize: "14px" }}>
            {errorMessage}
          </div>
        )}
      </motion.div>
    </div>
  );
};

export default SignIn;
