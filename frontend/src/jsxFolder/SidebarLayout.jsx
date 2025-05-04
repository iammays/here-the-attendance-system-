import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next'; // ✅ Import translation
import '../cssFolder/SidebarLayout.css';
import { FaPowerOff, FaCog, FaRegCommentDots, FaThLarge } from 'react-icons/fa';
import Logout from './Logout'; // ✅ make sure path is correct

const SidebarLayout = () => {
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const { t } = useTranslation(); // ✅ Use the translation hook

  return (
    <aside className="sidebar">
      <div className="sidebar-links">
        <NavLink to="/dashboard" className="sidebar-link">
          <FaThLarge /> {t('dashboard')}
        </NavLink>
        <br />
        <NavLink to="/wf_reports" className="sidebar-link">
          <FaRegCommentDots /> {t('wfReport')}
        </NavLink>
      </div>

      <div className="sidebar-bottom">
        <NavLink to="/settings" className="sidebar-link">
          <FaCog /> {t('settings')}
        </NavLink>

        <button onClick={() => setShowLogoutModal(true)} className="sidebar-link logout-btn">
          <FaPowerOff /> {t('logout')}
        </button>

        {/* Show modal */}
        {showLogoutModal && <Logout onClose={() => setShowLogoutModal(false)} />}
        <br />
      </div>
    </aside>
  );
};

export default SidebarLayout;