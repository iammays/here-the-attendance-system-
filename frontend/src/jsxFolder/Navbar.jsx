import React, { useState, useEffect } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Dropdown } from 'react-bootstrap';
import logo from './logo.png';
import '../cssFolder/Navbar.css';
import { useTranslation } from 'react-i18next';

const Navbar = () => {
  const { t, i18n } = useTranslation();
  const [language, setLanguage] = useState(localStorage.getItem('language') || 'en');

  const handleLanguageChange = (lang) => {
    setLanguage(lang);
    i18n.changeLanguage(lang);
    localStorage.setItem('language', lang);
    document.body.dir = lang === 'ar' ? 'rtl' : 'ltr';
  };

  useEffect(() => {
    i18n.changeLanguage(language);
    document.body.dir = language === 'ar' ? 'rtl' : 'ltr';
  }, []);

  return (
    <nav className="navbar navbar-expand-lg navbar-light bg-white shadow-sm px-4 py-3"   style={{ direction: 'ltr' }}>
      <div className="container-fluid">
        <img src={logo} alt="Logo" className="logo" />

        <div className="d-flex align-items-center ms-auto gap-3">
          {/* Language Dropdown */}
          <Dropdown>
            <Dropdown.Toggle
              variant="light"
              className="d-flex align-items-center border-0 p-0 bg-transparent"
            >
              {language === 'en' ? 'English' : 'العربية'}
            </Dropdown.Toggle>

            <Dropdown.Menu>
              <Dropdown.Item onClick={() => handleLanguageChange('en')}>{t('english')}</Dropdown.Item>
              <Dropdown.Item onClick={() => handleLanguageChange('ar')}>{t('arabic')}</Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
