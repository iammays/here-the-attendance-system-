import React, { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';

const Reset_Password = () => {
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // TODO: Add your logic here (API call or validation)
    console.log(formData);
  };

  return (
    <div className="d-flex justify-content-center align-items-center vh-100 bg-light">
      <div className="bg-white shadow rounded p-5" style={{ width: '100%', maxWidth: '400px' }}>
        <h3 className="text-center mb-2 fw-bold">Reset password</h3>
        <p className="text-center text-muted mb-4">Please type something you'll remember</p>
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label fw-semibold">Current password</label>
            <div className="input-group">
              <input
                type="password"
                className="form-control"
                name="currentPassword"
                placeholder="current password"
                onChange={handleChange}
              />
              <span className="input-group-text">
                <i className="bi bi-eye" />
              </span>
            </div>
          </div>
          <div className="mb-3">
            <label className="form-label fw-semibold">New password</label>
            <div className="input-group">
              <input
                type="password"
                className="form-control"
                name="newPassword"
                placeholder="must be 8 characters"
                onChange={handleChange}
              />
              <span className="input-group-text">
                <i className="bi bi-eye" />
              </span>
            </div>
          </div>
          <div className="mb-4">
            <label className="form-label fw-semibold">Confirm new password</label>
            <div className="input-group">
              <input
                type="password"
                className="form-control"
                name="confirmPassword"
                placeholder="repeat password"
                onChange={handleChange}
              />
              <span className="input-group-text">
                <i className="bi bi-eye" />
              </span>
            </div>
          </div>
          <button type="submit" className="btn btn-primary w-100">
            Reset password
          </button>
        </form>
      </div>
    </div>
  );
};

export default Reset_Password;
