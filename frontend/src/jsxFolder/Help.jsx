import React, { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../cssFolder/HelpAbout.css';

const Help = () => {
  const { t, i18n } = useTranslation();

  useEffect(() => {
    document.body.dir = i18n.language === 'ar' ? 'rtl' : 'ltr';
  }, [i18n.language]);

  return (
    <div className="page-container">
      <h2 className="page-title">{t('helpTitle')}</h2>
      <div className="page-content">
        <p>{t('helpIntro')}</p>
        <ul>
          <li>{t('helpStep1')}</li>
          <li>{t('helpStep2')}</li>
          <li>{t('helpStep3')}</li>
          <li>{t('helpStep4')}</li>
        </ul>
        <p>{t('helpContact')} <strong>{t('supportEmail')}</strong>.</p>
      </div>
    </div>
  );
};

export default Help;