import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const SignIn = () => {
  const [username, setUsername] = useState(""); // changed from email
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const navigate = useNavigate();

  // const handleLogin = async () => {
  //   const res = await fetch("http://localhost:8080/api/auth/signin", {
  //     method: "POST",
  //     credentials: "include",
  //     headers: {
  //       "Content-Type": "application/json",
  //     },
  //     body: JSON.stringify({ name: username, password }), // still uses `name`
  //   });

  //   if (res.ok) {
  //     const data = await res.json();
  //     localStorage.setItem(
  //       "teacher",
  //       JSON.stringify({
  //         id: data.id,
  //         name: data.name,
  //         email: data.email,
  //         accessToken: data.accessToken,
  //         tokenType: data.tokenType,
  //       })
  //     );
  //     navigate("/dashboard");
  //   } else {
  //     alert("Login failed!");
  //   }
  // };

  const handleLogin = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/auth/signin", {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ name: username, password }),
      });
  
      const data = await res.text(); 
  
      if (res.ok) {
        const parsedData = JSON.parse(data);
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
        setErrorMessage(data.error || "Login failed. Please try again.");
      }
    } catch (error) {
      console.error("Login error:", error);
      setErrorMessage("Something went wrong. Please try again later.");
    }
  };
  

  return (
    <div className="container d-flex justify-content-center align-items-center vh-100 bg-light">
      <div className="card shadow p-4" style={{ width: "100%", maxWidth: "400px" }}>
        <h2 className="text-center mb-4">Sign In</h2>
        <div className="mb-3">
          <label htmlFor="username" className="form-label fw-semibold">
            Username
          </label>
          <input
            type="text"
            id="username"
            className="form-control"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Enter your username"
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
        {errorMessage && (
          <div
            className="mt-3 text-center"
            style={{
              color: "#A01220FF",
              fontSize: "12px",
              textDecoration: "underline",
            }}
          >
            {errorMessage}
          </div>
        )}
      </div>
    </div>
  );
};

export default SignIn;
