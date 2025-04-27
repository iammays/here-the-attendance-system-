// import React, { useState } from "react";
// import { useNavigate } from "react-router-dom";

// const SignIn = () => {
//   const [username, setUsername] = useState("");
//   const [password, setPassword] = useState("");
//   const [errorMessage, setErrorMessage] = useState("");

//   const navigate = useNavigate();

//   const handleLogin = async () => {
//     try {
//       const res = await fetch("http://localhost:8080/api/auth/signin", {
//         method: "POST",
//         credentials: "include",
//         headers: {
//           "Content-Type": "application/json",
//         },
//         body: JSON.stringify({ name: username, password }),
//       });
  
//       if (res.ok) {
//         const parsedData = await res.json();
//         localStorage.setItem(
//           "teacher",
//           JSON.stringify({
//             id: parsedData.id,
//             name: parsedData.name,
//             email: parsedData.email,
//             accessToken: parsedData.accessToken,
//             tokenType: parsedData.tokenType,
//           })
//         );
//         navigate("/dashboard");
//       } else {
//         const errorText = await res.text();
//         setErrorMessage(errorText||"You need to enter the password and username before entering the system." || "Incorrect username or password.");
//       }
//     } catch (error) {
//       console.error("Login error:", error);
//       setErrorMessage("Something went wrong. Please try again later.");
//     }
//   };

//   return (
//     <div className="container d-flex justify-content-center align-items-center vh-100 bg-light">
//       <div className="card shadow p-4" style={{ width: "500px", maxWidth: "800px", minHeight: "550px" }}>
//         <h2 className="text-center mb-4" style={{ fontSize: "38px", fontWeight: "bold", color: "#2A4B70FF" }}
//         >Sign In</h2>
//         <div className="mb-3">
//           <label htmlFor="username" className="form-label fw-semibold" style={{ fontSize: "16px", fontWeight: "bold", color: "#2A4B70FF" }}>
//             Username
//           </label>
//           <input
//             type="text"
//             id="username"
//             className="form-control "
//             value={username}
//             onChange={(e) => setUsername(e.target.value)}
//             placeholder="Enter your username"
//           />
//         </div>
//         <div className="mb-4">
//           <label htmlFor="password" className="form-label fw-semibold" style={{ fontSize: "16px", fontWeight: "bold", color: "#2A4B70FF" }}>
//             Password
//           </label>
//           <input
//             type="password"
//             id="password"
//             className="form-control"
//             value={password}
//             onChange={(e) => setPassword(e.target.value)}
//             placeholder="Enter your password"
//           />
//         </div>
//         <button
//           onClick={handleLogin}
//           className="btn btn-primary w-100 fw-semibold" style={{ fontSize: "20px", fontWeight: "bold", color: "#FFFFFF" }}
//         >
//           Login
//         </button>
//         {errorMessage && (
//           <div
//             className="mt-3 text-center"
//             style={{
//               color: "#000000",
//               fontSize: "12px",
//               fontWeight: "bold",
//               top: "120px",
//               position: "relative",
//               // textDecoration: "underline"
//             }}
//           >
//             {errorMessage}
//           </div>
//         )}
//       </div>
//     </div>
//   );
// };

// export default SignIn;

import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion"; // استوردنا الحركة
import logo from "./logo.png";


const SignIn = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const navigate = useNavigate();

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
        setErrorMessage(
          errorText ||
            "You need to enter the password and username before entering the system." ||
            "Incorrect username or password."
        );
      }
    } catch (error) {
      console.error("Login error:", error);
      setErrorMessage("Something went wrong. Please try again later.");
    }
  };

  return (
    <div className="container d-flex justify-content-center align-items-center vh-100 bg-light">
      <div
        className="card shadow p-4"
        style={{ width: "500px", maxWidth: "800px", minHeight: "700px" }}
      >
        {/* Logo with animation */}
        <motion.div 
          className="text-center mb-4"
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.8 }}
        >
          <img
            src={logo} 
            alt="Logo"
            style={{ width: "120px", height: "120px", objectFit: "contain" }}
          />
        </motion.div>

        <h2
          className="text-center mb-4"
          style={{
            fontSize: "38px",
            fontWeight: "bold",
            color: "#2A4B70FF",
          }}
        >
          Sign In
        </h2>

        <div className="mb-3">
          <label
            htmlFor="username"
            className="form-label fw-semibold"
            style={{ fontSize: "16px", fontWeight: "bold", color: "#2A4B70FF" }}
          >
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
          <label
            htmlFor="password"
            className="form-label fw-semibold"
            style={{ fontSize: "16px", fontWeight: "bold", color: "#2A4B70FF" }}
          >
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
          style={{
            fontSize: "20px",
            fontWeight: "bold",
            color: "#FFFFFF",
          }}
        >
          Login
        </button>
        {errorMessage && (
          <div
            className="mt-3 text-center"
            style={{
              color: "#000000",
              fontSize: "12px",
              fontWeight: "bold",
              top: "120px",
              position: "relative",
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
