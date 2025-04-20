import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const SignIn = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleLogin = async () => {
    const res = await fetch("http://localhost:8080/api/auth/signin", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ name: email, password }),
    });

    if (res.ok) {
      const data = await res.json();
      localStorage.setItem(
        "teacher",
        JSON.stringify({
          id: data.id,
          name: data.name,
          email: data.email,
          accessToken: data.accessToken,
          tokenType: data.tokenType,
        })
      );
      navigate("/dashboard");
    } else {
      alert("Login failed!");
    }
  };

  return (
    <div className="container d-flex justify-content-center align-items-center vh-100 bg-light">
      <div className="card shadow p-4" style={{ width: "100%", maxWidth: "400px" }}>
        <h2 className="text-center mb-4">Sign In</h2>
        <div className="mb-3">
          <label htmlFor="email" className="form-label fw-semibold">
            Email
          </label>
          <input
            type="email"
            id="email"
            className="form-control"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Enter your email"
          />
        </div>
        <div className="mb-4">
          <label htmlFor="password" className="form-label fw-semibold">
            Password
          </label>
          <input
            type="password"
            id="password"
            className="form-control"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter your password"
          />
        </div>
        <button
          onClick={handleLogin}
          className="btn btn-primary w-100 fw-semibold"
        >
          Login
        </button>
      </div>
    </div>
  );
};

export default SignIn;
