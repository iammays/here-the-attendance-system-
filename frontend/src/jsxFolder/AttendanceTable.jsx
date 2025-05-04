import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useTranslation } from 'react-i18next';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../cssFolder/AttendanceTable.css';
import ActionButtons from './ActionButtons';

const AttendanceTable = () => {
  const { t, i18n } = useTranslation();
  const { courseId, lectureId } = useParams();
  const navigate = useNavigate();

  const [attendanceData, setAttendanceData] = useState([]);
  const [filteredData, setFilteredData] = useState([]);
  const [lectureInfo, setLectureInfo] = useState({
    date: '',
    startTime: '',
    endTime: '',
    day: '',
    courseName: '',
    credits: 0,
    category: '',
    roomId: '',
  });
  const [sortConfig, setSortConfig] = useState({ key: 'studentName', direction: 'asc' });
  const [error, setError] = useState(null);
  const [statusChanges, setStatusChanges] = useState({});
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(false);
  const [showLectureModal, setShowLectureModal] = useState(false);
  const [newLectureData, setNewLectureData] = useState({
    courseId,
    date: '',
    startTime: '',
    endTime: '',
    day: '',
    roomId: '',
  });
  const [modalError, setModalError] = useState(null);

  const getAuthHeaders = () => {
    const teacher = JSON.parse(localStorage.getItem('teacher'));
    if (!teacher?.accessToken) {
      console.error('No access token found in localStorage');
      setError(t('Login Required'));
      navigate('/login');
      return {};
    }
    return {
      Authorization: `Bearer ${teacher.accessToken}`,
      'Content-Type': 'application/json',
    };
  };

  useEffect(() => {
    document.body.dir = i18n.language === 'ar' ? 'rtl' : 'ltr';
  }, [i18n.language]);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError(null);
      try {
        const headers = getAuthHeaders();
        if (!headers.Authorization) return;

        const attendanceResponse = await axios.get(`http://localhost:8080/api/attendances/table/${lectureId}`, {
          headers,
          params: { sortBy: sortConfig.key, sortOrder: sortConfig.direction, search },
        });
        setAttendanceData(attendanceResponse.data);
        setFilteredData(attendanceResponse.data);

        const lectureResponse = await axios.get(`http://localhost:8080/courses/${lectureId}`, {
          headers,
        });
        const lectureData = lectureResponse.data;
        setLectureInfo({
          date: lectureId.split('-').slice(-4, -1).join('-') || 'Unknown',
          startTime: lectureData.startTime || 'Unknown',
          endTime: lectureData.endTime || 'Unknown',
          day: lectureData.day || 'Unknown',
          courseName: lectureData.name || courseId,
          credits: lectureData.credits || 0,
          category: lectureData.category || 'Unknown',
          roomId: lectureData.roomId || 'Unknown',
        });
      } catch (err) {
        const errorMessage = err.response?.status === 400 ? t('invalid Lecture Format') :
                            err.response?.status === 401 ? t('unauthorized') :
                            err.response?.status === 404 ? t('lecture Not Found') :
                            err.response?.status === 500 ? t('server Error') :
                            err.message;
        setError(`${t('fetch Error')}: ${errorMessage}`);
        console.error('Fetch error:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [courseId, lectureId, sortConfig, t, navigate]);

  useEffect(() => {
    let filtered = [...attendanceData];
    if (statusFilter) {
      filtered = filtered.filter(row => row.status === statusFilter || (statusFilter === 'Pending' && !row.status));
    }
    if (search) {
      filtered = filtered.filter(row =>
        row.studentId.toLowerCase().includes(search.toLowerCase()) ||
        row.studentName.toLowerCase().includes(search.toLowerCase())
      );
    }
    filtered.sort((a, b) => {
      let aValue = a[sortConfig.key] || '';
      let bValue = b[sortConfig.key] || '';
      if (sortConfig.key === 'first Detected At') {
        aValue = aValue === 'undetected' ? '' : aValue;
        bValue = bValue === 'undetected' ? '' : bValue;
      }
      return sortConfig.direction === 'asc' ?
        aValue.toString().localeCompare(bValue.toString()) :
        bValue.toString().localeCompare(aValue.toString());
    });
    setFilteredData(filtered);
  }, [statusFilter, search, attendanceData, sortConfig]);

  const handleSort = (key) => {
    setSortConfig(prev => ({
      key,
      direction: prev.key === key && prev.direction === 'asc' ? 'desc' : 'asc',
    }));
  };

  const handleStatusChange = (studentId, newStatus) => {
    setAttendanceData(prev =>
      prev.map(row =>
        row.studentId === studentId ? { ...row, status: newStatus } : row
      )
    );
    setStatusChanges(prev => ({ ...prev, [studentId]: newStatus }));
  };

  const handleFinalize = async () => {
    setLoading(true);
    setError(null);
    try {
      const updates = Object.keys(statusChanges).map(studentId => ({
        studentId,
        status: statusChanges[studentId],
      }));

      if (updates.length > 0) {
        await Promise.all(
          updates.map(update =>
            axios.put(
              `http://localhost:8080/api/attendances/updateWithEmail/${lectureId}/${update.studentId}`,
              { status: update.status },
              { headers: getAuthHeaders() }
            )
          )
        );
      }

      await axios.post(
        `http://localhost:8080/api/attendances/finalize/${lectureId}`,
        {},
        { headers: getAuthHeaders() }
      );

      const attendanceResponse = await axios.get(`http://localhost:8080/api/attendances/table/${lectureId}`, {
        headers: getAuthHeaders(),
        params: { sortBy: sortConfig.key, sortOrder: sortConfig.direction, search },
      });

      const updatedData = attendanceResponse.data.map(row => ({
        ...row,
        status: statusChanges[row.studentId] || row.status || 'Pending',
      }));

      setAttendanceData(updatedData);
      setFilteredData(updatedData);
      setStatusChanges({});
    } catch (err) {
      setError(`${t('finalize Error')}: ${err.response?.data || err.message}`);
      console.error('Error finalizing attendance:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleClearStatuses = async () => {
    setLoading(true);
    setError(null);
    try {
      await axios.delete(`http://localhost:8080/api/attendances/statuses/${lectureId}`, {
        headers: getAuthHeaders(),
      });
      const updatedData = attendanceData.map(row => ({ ...row, status: 'Pending' }));
      setAttendanceData(updatedData);
      setFilteredData(updatedData);
      setStatusChanges({});
    } catch (err) {
      setError(`${t('clear Statuses Error')}: ${err.response?.data || err.message}`);
      console.error('Error clearing statuses:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddLecture = async () => {
    try {
      setModalError(null);
      const { courseId, date, startTime, endTime, roomId } = newLectureData;
      if (!date || !startTime || !endTime) {
        setModalError(t('all Fields Required'));
        return;
      }

      const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
      const timeRegex = /^\d{2}:\d{2}$/;
      if (!dateRegex.test(date) || !timeRegex.test(startTime) || !timeRegex.test(endTime)) {
        setModalError(t('invalid Format'));
        return;
      }

      const lectureDate = new Date(date);
      const calculatedDay = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"][lectureDate.getDay()];

      const lectureData = {
        courseId,
        date,
        startTime,
        endTime,
        day: calculatedDay,
        roomId,
      };

      const response = await axios.post('http://localhost:8080/api/attendances/addNewLecture', lectureData, {
        headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
      });

      const newLectureId = response.data.lectureId;
      setShowLectureModal(false);
      setNewLectureData({ courseId, date: '', startTime: '', endTime: '', day: '', roomId: '' });
      navigate(`/attendance/${courseId}/${newLectureId}`);
    } catch (err) {
      setModalError(`${t('add Lecture Error')}: ${err.response?.data?.error || err.message}`);
      console.error('Add lecture error:', err);
    }
  };

  const formatFirstCheckTimes = (firstCheckTimes) => {
    if (!firstCheckTimes || firstCheckTimes.length === 0) return t('no Detections');
    const validCheckTimes = firstCheckTimes.filter(time => time !== 'un detected')
      .map((time, index) => `Session ${index + 1}: ${time}`);
    return validCheckTimes.length > 0 ? validCheckTimes.join(', ') : t('no Detections');
  };

  if (loading) return <div className="loading">{t('loading')}</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="wf-reports-wrapper">
      <h1 className="wf-title">{lectureInfo.courseName}</h1>

      <div className="row wf-filters">
        <div className="col-md-3">
          <select
            className="form-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            style={{ padding: '0.75rem 2rem' }}
          >
            <option value="">{t('Filter By')}</option>
            <option value="Pending">{t('pending')}</option>
            <option value="Present">{t('present')}</option>
            <option value="Absent">{t('absent')}</option>
            <option value="Late">{t('late')}</option>
            <option value="Excuse">{t('excuse')}</option>
          </select>
        </div>
        <div className="col-md-6">
          <input
            type="text"
            className="form-control"
            placeholder={t('Search Placeholder')}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="col-md-3" style={{ width: '250px',height: '40px' }}>
          <button
            className="btn btn-outline-secondary w-100"
            onClick={() => {
              setStatusFilter('');
              setSearch('');
            }}
          >
            {t('Reset Filter')}
          </button>
        </div>
      </div>

      <div className="course-info">
        <div className="d-flex justify-content-between align-items-start">
          <div></div>
          <p className="mb-1 text-center"><strong>{lectureInfo.day}</strong></p>
          <div className="text-right">
            <p className="mb-1"><strong>{lectureInfo.date}</strong></p>
            <p className="mb-0"><strong>{lectureInfo.startTime} - {lectureInfo.endTime}</strong></p>
          </div>
        </div>
      </div>

      {showLectureModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>{t('Add New Lecture')}</h2>
            {modalError && <div className="error">{modalError}</div>}
            <div className="form-group">
              <label>{t('Date')}</label>
              <input
                type="text"
                className="form-control"
                placeholder="YYYY-MM-DD"
                value={newLectureData.date}
                onChange={e => setNewLectureData({ ...newLectureData, date: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>{t('Start Time')}</label>
              <input
                type="text"
                className="form-control"
                placeholder="HH:MM"
                value={newLectureData.startTime}
                onChange={e => setNewLectureData({ ...newLectureData, startTime: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>{t('End Time')}</label>
              <input
                type="text"
                className="form-control"
                placeholder="HH:MM"
                value={newLectureData.endTime}
                onChange={e => setNewLectureData({ ...newLectureData, endTime: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>{t('Room Id')}</label>
              <input
                type="text"
                className="form-control"
                placeholder={t('roomId')}
                value={newLectureData.roomId}
                onChange={e => setNewLectureData({ ...newLectureData, roomId: e.target.value })}
              />
            </div>
            <div className="modal-actions">
              <button className="btn btn-primary" onClick={handleAddLecture}>
                {t('add')}
              </button>
              <button
                className="btn btn-secondary"
                onClick={() => {
                  setShowLectureModal(false);
                  setModalError(null);
                  setNewLectureData({ courseId, date: '', startTime: '', endTime: '', day: '', roomId: '' });
                }}
              >
                {t('cancel')}
              </button>
            </div>
          </div>
        </div>
      )}

      {filteredData.length === 0 ? (
        <div className="no-data">{t('No Attendance Records')}</div>
      ) : (
        <div className="table-responsive wf-table">
          <table className="table table-bordered table-striped">
            <thead>
              <tr>
                <th onClick={() => handleSort('studentName')}>
                  {t('Student Name')} {sortConfig.key === 'studentName' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                </th>
                <th colSpan="4" className="text-center">
                  {t('Status')} {sortConfig.key === 'status' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                </th>
                <th onClick={() => handleSort('firstDetectedAt')}>
                  {t('First Detected At')} {sortConfig.key === 'firstDetectedAt' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                </th>
                <th onClick={() => handleSort('detectionCount')}>
                  {t('Notes')} {sortConfig.key === 'detectionCount' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                </th>
              </tr>
              <tr>
                <th></th>
                <th className="text-center">P</th>
                <th className="text-center">A</th>
                <th className="text-center">L</th>
                <th className="text-center">E</th>
                <th></th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filteredData.map(row => (
                <tr key={row.studentId}>
                  <td>{row.studentName}</td>
                  <td>
                    <label className="radio-label present">
                      <input
                        type="radio"
                        name={`status-${row.studentId}`}
                        value="Present"
                        checked={row.status === 'Present'}
                        onChange={() => handleStatusChange(row.studentId, 'Present')}
                        disabled={loading}
                      />
                      <span></span>
                    </label>
                  </td>
                  <td>
                    <label className="radio-label absent">
                      <input
                        type="radio"
                        name={`status-${row.studentId}`}
                        value="Absent"
                        checked={row.status === 'Absent'}
                        onChange={() => handleStatusChange(row.studentId, 'Absent')}
                        disabled={loading}
                      />
                      <span></span>
                    </label>
                  </td>
                  <td>
                    <label className="radio-label late">
                      <input
                        type="radio"
                        name={`status-${row.studentId}`}
                        value="Late"
                        checked={row.status === 'Late'}
                        onChange={() => handleStatusChange(row.studentId, 'Late')}
                        disabled={loading}
                      />
                      <span></span>
                    </label>
                  </td>
                  <td>
                    <label className="radio-label excuse">
                      <input
                        type="radio"
                        name={`status-${row.studentId}`}
                        value="Excuse"
                        checked={row.status === 'Excuse'}
                        onChange={() => handleStatusChange(row.studentId, 'Excuse')}
                        disabled={loading}
                      />
                      <span></span>
                    </label>
                  </td>
                  <td>{row.firstDetectedAt === 'undetected' ? t('No Detections') : row.firstDetectedAt}</td>
                  <td className="notes-tooltip">
                    {formatFirstCheckTimes(row.firstCheckTimes)}
                    <span className="tooltip-text">{formatFirstCheckTimes(row.firstCheckTimes)}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ActionButtons
        onAddNewLecture={() => setShowLectureModal(true)}
        onExport={() => {}}
        onSave={handleFinalize}
        onClearStatuses={handleClearStatuses}
      />
    </div>
  );
};

export default AttendanceTable;