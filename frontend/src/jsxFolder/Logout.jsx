import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import '../cssFolder/Logout.css'; // ✅ Import the CSS

const Logout = () => {
  const [showPopup, setShowPopup] = useState(false);
  const navigate = useNavigate();
  const { t } = useTranslation();

  useEffect(() => {
    setShowPopup(true);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('teacher');
    localStorage.removeItem('language');
    navigate('/');
  };

  const cancelLogout = () => {
    setShowPopup(false);
    navigate('/dashboard');
  };

  return (
    <>
      {showPopup && (
        <div className="logout-backdrop">
          <div className="logout-modal">
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
              <button onClick={cancelLogout} className="btn btn-secondary">
                {t('noCancel') || "No, take me back"}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Logout;
