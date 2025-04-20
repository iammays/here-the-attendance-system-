import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const LogoutButton = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();

  const handleLogout = () => {
    localStorage.removeItem('teacher');
    localStorage.removeItem('language');
    navigate('/');
  };

  return (
    <button onClick={handleLogout} className="btn btn-outline-danger">
      {t('logout')}
    </button>
  );
};

export default LogoutButton;
