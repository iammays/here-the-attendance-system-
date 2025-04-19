import React, { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';

const Reset_Password = () => {
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // 🟢 Get teacher data from localStorage
  const teacher = JSON.parse(localStorage.getItem('teacher'));
  const teacherUsername = teacher?.name || '';
  const teacherId = teacher?.id || '';
  const token = teacher?.accessToken || '';

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
    setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const { currentPassword, newPassword, confirmPassword } = formData;

    if (newPassword !== confirmPassword) {
      setError('New passwords do not match.');
      return;
    }

    try {
      const validateResponse = await fetch('http://localhost:8080/teachers/validate-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
          name: teacherUsername,
          password: currentPassword
        })
      });

      if (!validateResponse.ok) {
        const errorMsg = await validateResponse.text();
        throw new Error(errorMsg || 'Invalid current password.');
      }

      const updateResponse = await fetch(`http://localhost:8080/teachers/${teacherId}/password`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
          oldPassword: currentPassword,
          newPassword: newPassword
        })
      });

      if (!updateResponse.ok) {
        const errorMsg = await updateResponse.text();
        throw new Error(errorMsg || 'Failed to update password.');
      }

      setSuccess('Password updated successfully!');
      setFormData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      });
    } catch (err) {
      setError(err.message || 'Something went wrong. Please try again.');
    }
  };

  return (
    <div className="d-flex justify-content-center align-items-center vh-100 bg-light">
      <div className="bg-white shadow rounded p-5" style={{ width: '100%', maxWidth: '400px' }}>
        <h3 className="text-center mb-2 fw-bold">Reset password</h3>
        <p className="text-center text-muted mb-4">Please type something you'll remember</p>

        {error && <div className="alert alert-danger">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label fw-semibold">Current password</label>
            <div className="input-group">
              <input
                type="password"
                className="form-control"
                name="currentPassword"
                placeholder="current password"
                value={formData.currentPassword}
                onChange={handleChange}
              />
              <span className="input-group-text"><i className="bi bi-eye" /></span>
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
                value={formData.newPassword}
                onChange={handleChange}
              />
              <span className="input-group-text"><i className="bi bi-eye" /></span>
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
                value={formData.confirmPassword}
                onChange={handleChange}
              />
              <span className="input-group-text"><i className="bi bi-eye" /></span>
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
