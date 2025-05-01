import React from 'react';
import { useNavigate } from 'react-router-dom';

const NotFound = () => {
  const navigate = useNavigate();

  return (
    <div className="d-flex flex-column justify-content-center align-items-center vh-100">
      <h1 className="display-4 mb-3">404 - Page Not Found</h1>
      <button className="btn btn-primary" onClick={() => navigate('/')}>
        Go to Sign In
      </button>
    </div>
  );
};

export default NotFound;