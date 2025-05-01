import React from "react";
import { Routes, Route, useLocation } from "react-router-dom";
import SignIn from "./jsxFolder/SignIn";
import Dashboard from "./jsxFolder/Dashboard";
import CourseDashboard from "./jsxFolder/CourseDashboard";
import Reset_Password from './jsxFolder/ResetPassword';
import Settings from './jsxFolder/Settings';
import Help from './jsxFolder/Help';
import About from './jsxFolder/About';
import WF_Reports from './jsxFolder/WFReports';
import Navbar from './jsxFolder/Navbar';
import Sidebar from './jsxFolder/SidebarLayout'; // ✅ just a component now
import Logout from './jsxFolder/Logout';
import NotFound from './jsxFolder/NotFound';
import ProtectedRoute from './jsxFolder/ProtectedRoute';
import AttendanceTable from './jsxFolder/AttendanceTable';
import ForgotPassword from "./jsxFolder/ForgotPassword";

const App = () => {
  const location = useLocation();

  const hideOn = ['/', '/logout', '/404', '/reset_password', '/ForgotPassword'];
  const shouldHideUI = hideOn.includes(location.pathname);

  return (
    <>
      {!shouldHideUI && <Navbar />}
      {!shouldHideUI && <Sidebar />}

      <Routes>
        <Route path="/ForgotPassword" element={<ForgotPassword />} />
      </Routes>

      {shouldHideUI ? (
        <Routes>
          <Route path="/" element={<SignIn />} />
          <Route path="/logout" element={<ProtectedRoute><Logout /></ProtectedRoute>} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      ) : (
        <div className="main-content">
          <Routes>
            {/* Protected Routes */}
            <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            <Route path="/course/:courseName" element={<ProtectedRoute><CourseDashboard /></ProtectedRoute>} />
            <Route path="/schedule/:courseName/:week/:day" element={<ProtectedRoute><div>Schedule Page</div></ProtectedRoute>} />
            <Route path="/reset_password" element={<ProtectedRoute><Reset_Password /></ProtectedRoute>} />
            <Route path="/settings" element={<ProtectedRoute><Settings /></ProtectedRoute>} />
            <Route path="/help" element={<ProtectedRoute><Help /></ProtectedRoute>} />
            <Route path="/about" element={<ProtectedRoute><About /></ProtectedRoute>} />
            <Route path="/wf_reports" element={<ProtectedRoute><WF_Reports /></ProtectedRoute>} />
            <Route path="/attendance/:courseId/:lectureId" element={<ProtectedRoute><AttendanceTable /></ProtectedRoute>} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </div>
      )}
    </>
  );
};

export default App;
