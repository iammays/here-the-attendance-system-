import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../cssFolder/HelpAbout.css';

const About = () => {
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();

  useEffect(() => {
    const teacher = JSON.parse(localStorage.getItem('teacher'));
    if (!teacher || !teacher.accessToken) {
      navigate('/'); // Redirect to login if not authenticated
    }
    document.body.dir = i18n.language === 'ar' ? 'rtl' : 'ltr'; // Adjust text direction based on language
  }, [navigate, i18n.language]);

  return (
    <div className="page-container">
      <h2 className="page-title">{t('aboutTitle')}</h2>
      <div className="page-content">
        <p>
          <strong>HERE - Attendance System</strong> {t('aboutLine1')}
        </p>
        <p>{t('aboutLine2')}</p>
        <p>{t('aboutLine3')} <strong>{t('email')}</strong>.</p>
      </div>
    </div>
  );
};

export default About;