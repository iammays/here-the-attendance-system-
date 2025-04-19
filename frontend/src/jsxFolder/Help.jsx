import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../cssFolder/HelpAbout.css';

const Help = () => {
  return (
    <div className="page-container">
      <h2 className="page-title">Help & Support</h2>
      <div className="page-content">
        <p>If you're experiencing issues or need assistance using the system, please try the following steps:</p>
        <ul>
          <li>Make sure you are connected to the internet.</li>
          <li>Ensure your browser is up to date.</li>
          <li>Clear your cache and cookies.</li>
          <li>Use the navigation menu to find your way back to the dashboard.</li>
        </ul>
        <p>Still need help? Contact your system administrator or email <strong>support@here-system.com</strong>.</p>
      </div>
    </div>
  );
};

export default Help;