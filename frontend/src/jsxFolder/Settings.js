import React, { useState, useEffect } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../cssFolder/Settings.css';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const Settings = () => {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();

  // Read the saved language from localStorage (or default to 'en')
  const [language, setLanguage] = useState(localStorage.getItem('language') || 'en');

  // On language change
  const handleLanguageChange = (e) => {
    const selectedLang = e.target.value;
    setLanguage(selectedLang);
    i18n.changeLanguage(selectedLang);
    localStorage.setItem('language', selectedLang);
    document.body.dir = selectedLang === 'ar' ? 'rtl' : 'ltr';
  };

  // On first load, ensure i18n uses the saved language
  useEffect(() => {
    i18n.changeLanguage(language);
    document.body.dir = language === 'ar' ? 'rtl' : 'ltr';
  }, []);

  return (
    <div className="d-flex flex-column align-items-center py-5 bg-light vh-100" >
      <div className="settings-container bg-white rounded shadow p-4" style={{ maxWidth: '600px', width: '100%' }}>
        <h2 className="text-center fw-bold mb-4">{t('settings')}</h2>
  
        <div className="mb-4">
          <label className="form-label fw-semibold">{t('changeLanguage')}</label>
          <select
  className={`form-select ${language === 'ar' ? 'text-end' : 'text-start'}`}
  style={{ direction: language === 'ar' ? 'rtl' : 'ltr'}}
 
  value={language}
  onChange={handleLanguageChange}
>
 
    <option  value="en">{t('english')}</option>
<option  value="ar">{t('arabic')}</option>
 

</select>

        </div>
  
        <hr />
  
        <div className="d-flex flex-column gap-3 mt-4">
          <button onClick={() => navigate('/Reset_Password')}  className="btn btn-outline-primary text-start">
            {t('changePassword')}
          </button>
  
          <button onClick={() => navigate('/help')} className="btn btn-outline-secondary text-start">
            {t('help')}
          </button>
  
          <button onClick={() => navigate('/about')} className="btn btn-outline-dark text-start">
            {t('aboutUs')}
          </button>
        </div>
      </div>
    </div>
  );
  
};

export default Settings;
