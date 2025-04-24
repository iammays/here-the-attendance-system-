
import React, { useEffect, useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../cssFolder/WFReports.css'; // Import the custom styles

const WFReports = () => {
  const [reports, setReports] = useState([]);
  const [filteredReports, setFilteredReports] = useState([]);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(false);

  const teacherData = JSON.parse(localStorage.getItem('teacher'));
  const token = teacherData?.accessToken;
  const teacherId = teacherData?.id;

  useEffect(() => {
    if (!teacherId || !token) return;
    setLoading(true);
    fetch(`http://localhost:8080/api/wf-reports/teacher/${teacherId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then(res => res.json())
      .then(data => {
        setReports(data);
        setFilteredReports(data);
        setLoading(false);
      })
      .catch(err => {
        console.error(err);
        setLoading(false);
      });
  }, [teacherId, token]);

  useEffect(() => {
    let filtered = reports;

    if (statusFilter) {
      filtered = filtered.filter(r => r.status === statusFilter);
    }

    if (search) {
      const searchLower = search.toLowerCase();
      filtered = filtered.filter(r =>
        Object.values(r).some(value =>
          String(value).toLowerCase().includes(searchLower)
        )
      );
    }

    setFilteredReports(filtered);
  }, [search, statusFilter, reports]);

  const handleAction = async (studentId, courseId, action) => {
    const url = action === 'approve'
      ? 'http://localhost:8080/api/attendances/approve-wf'
      : 'http://localhost:8080/api/attendances/ignore-wf';

    setLoading(true);

    try {
      const res = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ studentId, courseId })
      });

      if (res.ok) {
        const updatedReports = reports.map(r =>
          r.studentId === studentId && r.courseId === courseId
            ? { ...r, status: action === 'approve' ? 'Approved' : 'Ignored' }
            : r
        );
        setReports(updatedReports);
        setLoading(false);
      } else {
        const errorText = await res.text();
        alert(`Failed to ${action} WF: ${errorText}`);
        setLoading(false);
      }
    } catch (error) {
      alert('Network error. Try again later.');
      setLoading(false);
    }
  };

  return (
    <div className="wf-reports-wrapper">
      <h1 className="wf-title">WF Reports</h1>

      {loading && (
        <div className="wf-spinner">
          <div className="spinner-border text-primary" role="status" />
        </div>
      )}

      <div className="row wf-filters">
        <div className="col-md-3">
          <select className="form-select" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="">Filter By</option>
            <option value="Ignored">Ignored</option>
            <option value="Approved">Approved</option>
            <option value="Pending">Pending</option>
          </select>
        </div>
        <div className="col-md-6">
          <input
            type="text"
            className="form-control"
            placeholder="Search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="col-md-3">
          <button className="btn btn-outline-secondary w-100" onClick={() => {
            setStatusFilter('');
            setSearch('');
          }}>
            Reset Filter
          </button>
        </div>
      </div>

      <div className="table-responsive wf-table">
        <table className="table table-bordered table-striped">
          <thead>
            <tr>
              <th>Student ID</th>
              <th>Student Name</th>
              <th>Course Name</th>
              <th>Teacher Name</th>
              <th>Date</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredReports.map((report, index) => (
              <tr key={index}>
                <td>{report.studentId}</td>
                <td>{report.studentName}</td>
                <td>{report.courseName}</td>
                <td>{report.teacherName}</td>
                <td>{report.date}</td>
                <td>
                  <span className={`badge ${
                    report.status === 'Approved'
                      ? 'bg-success'
                      : report.status === 'Pending'
                      ? 'bg-secondary'
                      : 'bg-warning text-dark'
                  }`}>
                    {report.status}
                  </span>
                </td>
                <td>
                  {report.status === 'Pending' ? (
                    <>
                      <button
                        className="btn btn-success btn-sm me-2"
                        disabled={loading}
                        onClick={() => handleAction(report.studentId, report.courseId, 'approve')}
                      >
                        Approve
                      </button>
                      <button
                        className="btn btn-danger btn-sm"
                        disabled={loading}
                        onClick={() => handleAction(report.studentId, report.courseId, 'ignore')}
                      >
                        Ignore
                      </button>
                    </>
                  ) : (
                    <span className="text-muted">{report.status}</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default WFReports;
