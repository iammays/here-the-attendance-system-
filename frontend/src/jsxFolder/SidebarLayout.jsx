import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import '../cssFolder/SidebarLayout.css';
import { FaPowerOff, FaCog, FaRegBell, FaThLarge, FaRegCommentDots } from 'react-icons/fa';
import Logout from './Logout'; // ✅ make sure path is correct

const SidebarLayout = () => {
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  return (
    <aside className="sidebar">
      <div className="sidebar-links">
        <NavLink to="/dashboard" className="sidebar-link"><FaThLarge /> Dashboard</NavLink>
        <br />
        <NavLink to="/wf_reports" className="sidebar-link"><FaRegCommentDots /> WF Report</NavLink>
      </div>

      <div className="sidebar-bottom">
        <NavLink to="/settings" className="sidebar-link"><FaCog /> Settings</NavLink>

        <button onClick={() => setShowLogoutModal(true)} className="sidebar-link logout-btn">
          <FaPowerOff /> Logout
        </button>

        {/* Show modal */}
        {showLogoutModal && <Logout onClose={() => setShowLogoutModal(false)} />}
          <br />
      </div>
    </aside>
  );
};

export default SidebarLayout;
