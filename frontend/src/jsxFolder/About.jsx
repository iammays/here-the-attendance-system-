import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../cssFolder/HelpAbout.css';

const About = () => {
  return (
    <div className="page-container">
      <h2 className="page-title">About Us</h2>
      <div className="page-content">
        <p>
          <strong>HERE - Attendance System</strong> is a modern solution designed to automate and manage student attendance using face recognition technology.
        </p>
        <p>
          Developed by a dedicated student team, this platform ensures accuracy, ease of use, and real-time tracking across educational institutions.
        </p>
        <p>
          For more information, visit our website or contact our team at <strong>info@here-system.com</strong>.
        </p>
      </div>
    </div>
  );
};

export default About;
