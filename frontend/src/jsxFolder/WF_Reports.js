import React, { useEffect, useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';

const WFReports = () => {
  const [reports, setReports] = useState([]);
  const [filteredReports, setFilteredReports] = useState([]);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    const staticData = [
      {
        studentId: '202019194',
        studentName: 'Mays Albutmah',
        courseName: 'Swer111',
        teacherName: 'Mohammad Abu Ayash',
        date: '14/01/2024',
        status: 'Approved'
      },
      {
        studentId: '202019194',
        studentName: 'Mays Albutmah',
        courseName: 'Swer111',
        teacherName: 'Mohammad Abu Ayash',
        date: '14/01/2024',
        status: 'Pending'
      },
      // Add more static records as needed
    ];
    setReports(staticData);
    setFilteredReports(staticData);
  }, []);

  useEffect(() => {
    let filtered = reports;

    if (statusFilter) {
      filtered = filtered.filter(r => r.status === statusFilter);
    }

    if (search) {
      filtered = filtered.filter(r =>
        r.studentName.toLowerCase().includes(search.toLowerCase()) ||
        r.courseName.toLowerCase().includes(search.toLowerCase())
      );
    }

    setFilteredReports(filtered);
  }, [search, statusFilter, reports]);

  return (
    <div className="container mt-5">
      <h1 className="mb-4">WF Reports</h1>
      <div className="row mb-3">
        <div className="col-md-3">
          <select className="form-select" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="">Filter By</option>
            <option value="Binding">Binding</option>
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

      <div className="table-responsive">
        <table className="table table-bordered table-striped">
          <thead className="table-light">
            <tr>
              <th>Student ID</th>
              <th>Student Name</th>
              <th>Course Name</th>
              <th>Teacher Name</th>
              <th>Date</th>
              <th>Status</th>
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
                    report.status === 'Approved' ? 'bg-success' :
                    report.status === 'Pending' ? 'bg-secondary' :
                    'bg-warning text-dark'
                  }`}>
                    {report.status}
                  </span>
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
