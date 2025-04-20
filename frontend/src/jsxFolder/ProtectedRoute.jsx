import React from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
  const teacher = JSON.parse(localStorage.getItem('teacher'));
  return teacher && teacher.accessToken ? children : <Navigate to="/" />;
};

export default ProtectedRoute;
