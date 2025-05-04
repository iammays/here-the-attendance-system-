import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";

const ForgotPassword = () => {
  const [username, setUsername] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const navigate = useNavigate();

  const handleValidateAndChangePassword = async () => {
    if (!username || !newPassword) {
      setErrorMessage("Please fill all the fields.");
      return;
    }

    try {
      const storedTeacher = JSON.parse(localStorage.getItem("teacher"));
      const teacherId = storedTeacher?.id;

      if (!teacherId) {
        setErrorMessage("Teacher ID not found. Please login again.");
        return;
      }

      const updateRes = await fetch(`http://localhost:8080/teachers/api/${teacherId}/password`, {
        method: "PUT",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ newPassword }),
      });

      const responseText = await updateRes.text();

      if (updateRes.ok) {
        setSuccessMessage(responseText); 
        setErrorMessage("");
        setTimeout(() => navigate("/"), 2000);
      } else {
        setErrorMessage(responseText);
        setSuccessMessage("");
      }

    } catch (error) {
      console.error("Error:", error);
      setErrorMessage("Something went wrong. Please try again.");
    }
  };

  return (
    <div className="d-flex justify-content-center align-items-center min-vh-100 bg-light">
      <motion.div
        className="p-5 rounded-4 shadow-lg bg-white"
        style={{ width: "100%", maxWidth: "450px" }}
        initial={{ opacity: 0, y: -30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <h3 className="text-center mb-4 fw-bold" style={{ color: "#2A4B70" }}>
          Reset Password
        </h3>

        <div className="form-group mb-3">
          <label htmlFor="username" className="form-label text-muted">Username</label>
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

        <div className="form-group mb-4">
          <label htmlFor="newPassword" className="form-label text-muted">New Password</label>
          <input
            type="password"
            id="newPassword"
            className="form-control p-3 rounded-3"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder="Enter your new password"
            style={{ backgroundColor: "#f8f9fa" }}
          />
        </div>

        <button
          onClick={handleValidateAndChangePassword}
          className="btn w-100 py-3 rounded-3 fw-bold"
          style={{ backgroundColor: "#2A4B70", color: "#fff", fontSize: "18px" }}
        >
          Reset Password
        </button>

        {errorMessage && (
          <div className="alert alert-danger text-center mt-3 p-2 rounded-3" style={{ fontSize: "14px" }}>
            {errorMessage}
          </div>
        )}

        {successMessage && (
          <div className="alert alert-success text-center mt-3 p-2 rounded-3" style={{ fontSize: "14px" }}>
            {successMessage}
          </div>
        )}
      </motion.div>
    </div>
  );
};

export default ForgotPassword;
