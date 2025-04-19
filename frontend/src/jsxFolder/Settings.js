import React, { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../cssFolder/Settings.css';
import { useNavigate } from 'react-router-dom';

const Settings = () => {
  const [language, setLanguage] = useState('en');
  const navigate = useNavigate();

  const handleLanguageChange = (e) => {
    setLanguage(e.target.value);
    // You can add i18n language change logic here if needed
  };

  return (
    <div className="d-flex flex-column align-items-center py-5 bg-light vh-100">
      <div className="settings-container bg-white rounded shadow p-4" style={{ maxWidth: '600px', width: '100%' }}>
        <h2 className="text-center fw-bold mb-4">Settings</h2>

        <div className="mb-4">
          <label className="form-label fw-semibold">Change Language</label>
          <select
            className="form-select"
            value={language}
            onChange={handleLanguageChange}
          >
            <option value="en">English</option>
            <option value="ar">Arabic</option>
          </select>
        </div>

        <hr />

        <div className="d-flex flex-column gap-3 mt-4">
          <button onClick={() => navigate('/Reset_Password')} className="btn btn-outline-primary text-start">
            Change Password
          </button>

          <button onClick={() => navigate('/help')} className="btn btn-outline-secondary text-start">
            Help
          </button>

          <button onClick={() => navigate('/about')} className="btn btn-outline-dark text-start">
            About Us
          </button>
        </div>
      </div>
    </div>
  );
};

export default Settings;
