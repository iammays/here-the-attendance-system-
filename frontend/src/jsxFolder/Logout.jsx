import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import '../cssFolder/Logout.css';

const Logout = ({ onClose }) => {
  const navigate = useNavigate();
  const { t } = useTranslation();

  const handleLogout = () => {
    localStorage.removeItem('teacher');
    localStorage.removeItem('language');
    navigate('/');
  };

  return (
    <div className="logout-backdrop" onClick={onClose}>
      <div className="logout-modal" onClick={(e) => e.stopPropagation()}>
        <h3 className="logout-title">
          {t('areYouSure') || 'Are you sure?'}
        </h3>
        <p className="logout-text">
          {t('logoutConfirmation') || 'Are you sure you want to log out?'}
        </p>
        <div className="logout-actions">
          <button onClick={handleLogout} className="btn btn-danger">
            {t('yesLogout') || 'Yes, log me out'}
          </button>
          <button onClick={onClose} className="btn btn-secondary">
            {t('noCancel') || "No, take me back"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default Logout;