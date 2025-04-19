import React from "react";
import { Routes, Route } from "react-router-dom";
import SignIn from "./jsxFolder/SignIn";
import Dashboard from "./jsxFolder/Dashboard";
import CourseDashboard from "./jsxFolder/CourseDashboard";
import Reset_Password from './jsxFolder/Reset_Password';
import Settings from './jsxFolder/Settings';
import Help from './jsxFolder/Help';
import About from './jsxFolder/About';
import WF_Reports from './jsxFolder/WF_Reports';
import Navbar from './jsxFolder/Navbar';


const App = () => {
  return (
    <Routes>
      {/* <Route path="/" element={<SignIn />} /> */}
      <Route path="/Dashboard" element={<Dashboard />} />
      <Route path="/course/:courseName" element={<CourseDashboard />} />
      <Route path="/schedule/:courseName/:week/:day" element={<div>Schedule Page</div>} />
      {/* <Route path="/" element={<Reset_Password />} /> */}
      {/* <Route path="/" element={<Settings />} />  */}
      {/* <Route path="/help" element={<Help />} /> */}
      {/* <Route path="/about" element={<About />} /> */}
      {/* <Route path="/" element={<WF_Reports />} /> */}
      <Route path="/" element={<Navbar />} />

    </Routes>
  );
};

export default App;