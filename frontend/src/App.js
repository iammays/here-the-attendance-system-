import React from "react";
import { Routes, Route } from "react-router-dom";
import SignIn from "./jsxFolder/SignIn";
import Dashboard from "./jsxFolder/Dashboard";
import CourseDashboard from "./jsxFolder/CourseDashboard";

const App = () => {
  return (
    <Routes>
      <Route path="/" element={<SignIn />} />
      <Route path="/Dashboard" element={<Dashboard />} />
      <Route path="/course/:courseName" element={<CourseDashboard />} />
      <Route path="/schedule/:courseName/:week/:day" element={<div>Schedule Page</div>} />
    </Routes>
  );
};

export default App;