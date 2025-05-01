import React, { useState, useEffect } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../cssFolder/Settings.css';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Logout from './Logout'; // make sure the path is correct

const Settings = () => {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const [language, setLanguage] = useState(localStorage.getItem('language') || 'en');
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const handleLanguageChange = (e) => {
    const selectedLang = e.target.value;
    setLanguage(selectedLang);
    i18n.changeLanguage(selectedLang);
    localStorage.setItem('language', selectedLang);
    document.body.dir = selectedLang === 'ar' ? 'rtl' : 'ltr';
  };

  useEffect(() => {
    i18n.changeLanguage(language);
    document.body.dir = language === 'ar' ? 'rtl' : 'ltr';
  }, []);

  return (
    <div className="settings-wrapper py-5 px-4">
      <div className="settings-card">
        <h2 className="fw-bold mb-4">{t('settings')}</h2>

        <div className="mb-4">
  <label className="form-label fw-semibold">{t('changeLanguage')}</label>
  <select
    className={`form-select ${language === 'ar' ? 'text-end' : 'text-start'}`}
    style={{
      direction: language === 'ar' ? 'rtl' : 'ltr',
      padding: '0.75rem 2rem' // ✅ Added vertical and horizontal padding
    }}
    value={language}
    onChange={handleLanguageChange}
  >
    <option value="en">{t('english')}</option>
    <option value="ar">{t('arabic')}</option>
  </select>
</div>


        <hr />

        <div className="d-flex flex-column gap-3 mt-4">
  <button onClick={() => navigate('/Reset_Password')} className="btn custom-btn text-start">
    {t('changePassword')}
  </button>
  <button onClick={() => navigate('/help')} className="btn custom-btn text-start">
    {t('help')}
  </button>
  <button onClick={() => navigate('/about')} className="btn custom-btn text-start">
    {t('aboutUs')}
  </button>
  <button onClick={() => setShowLogoutModal(true)} className="btn custom-btn text-start">
    {t('logout') || 'Logout'}
  </button>
</div>

{showLogoutModal && <Logout onClose={() => setShowLogoutModal(false)} />}

        </div>
      </div>
  );
};

export default Settings;