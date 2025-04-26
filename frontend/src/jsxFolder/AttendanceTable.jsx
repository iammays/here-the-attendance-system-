import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import SearchBar from './SearchBar';
import LectureNavigation from './LectureNavigation';
import ActionButtons from './ActionButtons';
import { useTranslation } from 'react-i18next';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../cssFolder/AttendanceTable.css';

const AttendanceTable = () => {
  const { t, i18n } = useTranslation();
  const { courseId, lectureId } = useParams();
  const navigate = useNavigate();

  const [attendanceData, setAttendanceData] = useState([]);
  const [filteredData, setFilteredData] = useState([]);
  const [lectureInfo, setLectureInfo] = useState({ date: '', startTime: '', endTime: '', day: '', courseName: '' });
  const [lectures, setLectures] = useState([]);
  const [currentLectureIndex, setCurrentLectureIndex] = useState(0);
  const [sortConfig, setSortConfig] = useState({ key: 'studentId', direction: 'asc' });
  const [error, setError] = useState(null);
  const [statusChanges, setStatusChanges] = useState({});
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const getAuthHeaders = () => {
    const teacher = JSON.parse(localStorage.getItem('teacher'));
    if (!teacher?.accessToken) {
      console.error('No access token found in localStorage');
      setError(t('loginRequired'));
      return {};
    }
    return { Authorization: `Bearer ${teacher.accessToken}` };
  };

  useEffect(() => {
    document.body.dir = i18n.language === 'ar' ? 'rtl' : 'ltr';
  }, [i18n.language]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const lectureResponse = await axios.get(`http://localhost:8080/courses/${courseId}`, {
          headers: getAuthHeaders(),
        });
        const lectureData = lectureResponse.data;
        setLectureInfo({
          date: lectureId.split('-')[1] || lectureData.date || 'Unknown',
          startTime: lectureData.startTime || 'Unknown',
          endTime: lectureData.endTime || 'Unknown',
          day: lectureData.day || 'Unknown',
          courseName: lectureData.name || 'Unknown',
        });

        const attendanceResponse = await axios.get(`http://localhost:8080/api/attendances/table/${lectureId}`, {
          headers: getAuthHeaders(),
        });
        const data = attendanceResponse.data;
        setAttendanceData(data);
        setFilteredData(data);

        const day = lectureData.day || 'Monday';
        const lecturesResponse = await axios.get(`http://localhost:8080/courses/day/${day}`, {
          headers: getAuthHeaders(),
        });
        const courseLectures = lecturesResponse.data.filter(lecture => lecture.courseId === courseId);
        setLectures(courseLectures);
        const index = courseLectures.findIndex(lecture => lecture.lectureId === lectureId);
        setCurrentLectureIndex(index >= 0 ? index : 0);
      } catch (err) {
        setError(`${t('fetchError')}: ${err.response?.status === 401 ? t('unauthorized') : err.message}`);
        console.error('Fetch error:', err);
      }
    };
    fetchData();
  }, [courseId, lectureId]);

  useEffect(() => {
    let filtered = attendanceData;
    if (statusFilter) filtered = filtered.filter(r => r.status === statusFilter);
    if (search) {
      const lower = search.toLowerCase();
      filtered = filtered.filter(row =>
        row.studentId.toLowerCase().includes(lower) ||
        row.studentName.toLowerCase().includes(lower)
      );
    }
    setFilteredData(filtered);
  }, [search, statusFilter, attendanceData]);

  const handleStatusChange = (studentId, newStatus) => {
    setStatusChanges(prev => ({ ...prev, [studentId]: newStatus }));
    setFilteredData(prev => prev.map(row => row.studentId === studentId ? { ...row, status: newStatus } : row));
  };

  const handleSave = async () => {
    try {
      const updatePromises = Object.entries(statusChanges).map(([studentId, newStatus]) =>
        axios.put(`http://localhost:8080/api/attendances/updateWithEmail/${lectureId}/${studentId}`,
          { status: newStatus }, { headers: getAuthHeaders() })
      );
      await Promise.all(updatePromises);
      setAttendanceData(prev => prev.map(row => ({ ...row, status: statusChanges[row.studentId] || row.status })));
      setStatusChanges({});
      alert(t('saveSuccess'));
    } catch (err) {
      setError(`${t('saveError')}: ${err.response?.data || err.message}`);
      console.error('Save error:', err);
    }
  };

  const handleSort = (key) => {
    const direction = sortConfig.key === key && sortConfig.direction === 'asc' ? 'desc' : 'asc';
    setSortConfig({ key, direction });
    const sortedData = [...filteredData].sort((a, b) => {
      let aValue = a[key];
      let bValue = b[key];
      if (key === 'firstDetectedAt') {
        aValue = aValue === 'undetected' ? '' : aValue;
        bValue = bValue === 'undetected' ? '' : bValue;
      }
      if (aValue < bValue) return direction === 'asc' ? -1 : 1;
      if (aValue > bValue) return direction === 'asc' ? 1 : -1;
      return 0;
    });
    setFilteredData(sortedData);
  };

  const handleNavigateLecture = (direction) => {
    const newIndex = direction === 'next' ? currentLectureIndex + 1 : currentLectureIndex - 1;
    if (newIndex >= 0 && newIndex < lectures.length) {
      const newLectureId = lectures[newIndex].lectureId;
      setCurrentLectureIndex(newIndex);
      navigate(`/attendance/${courseId}/${newLectureId}`);
    }
  };

  const formatFirstCheckTimes = (firstCheckTimes) => {
    if (!firstCheckTimes || firstCheckTimes.length === 0) return t('noDetections');
    const validCheckTimes = firstCheckTimes.filter(check => check.firstCheckTime !== 'undetected')
      .map(check => `Session ${check.sessionId}: ${check.firstCheckTime}`);
    return validCheckTimes.length > 0 ? validCheckTimes.join(', ') : t('noDetections');
  };

  if (error) return <div className="error">{error}</div>;

  return (
    <div className="wf-reports-wrapper">
      <h1 className="wf-title">{lectureInfo.courseName}</h1>

      <div className="mb-4">
        <p><strong>{t('day')}:</strong> {lectureInfo.day}</p>
        <p><strong>{t('startTime')}:</strong> {lectureInfo.startTime}</p>
      </div>

      <LectureNavigation
        lectureInfo={lectureInfo}
        currentLectureIndex={currentLectureIndex}
        totalLectures={lectures.length}
        onNavigate={handleNavigateLecture}
      />

      <div className="row wf-filters">
        <div className="col-md-3">
          <select
            className="form-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            style={{
              padding: '0.75rem 2rem' // ✅ Added vertical and horizontal padding
            }}
          >
            <option value="">{t('filterBy')}</option>
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
            placeholder={t('searchPlaceholder')}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="col-md-3">
          <button
            className="btn btn-outline-secondary w-100"
            onClick={() => {
              setStatusFilter('');
              setSearch('');
            }}
          >
            {t('resetFilter')}
          </button>
        </div>
      </div>

      <div className="table-responsive wf-table">
        <table className="table table-bordered table-striped">
          <thead>
            <tr>
              <th onClick={() => handleSort('studentName')}>{t('studentName')}</th>
              <th onClick={() => handleSort('status')}>{t('status')}</th>
              <th onClick={() => handleSort('firstDetectedAt')}>{t('firstDetectedAt')}</th>
              <th onClick={() => handleSort('detectionCount')}>{t('notes')}</th>
            </tr>
          </thead>
          <tbody>
            {filteredData.map(row => (
              <tr key={row.studentId}>
                <td>{row.studentName}</td>
                <td>
                  <div className="status-radio">
                    {['Present', 'Absent', 'Late', 'Excuse'].map(status => (
                      <label key={status}>
                        <input
                          type="radio"
                          name={`status-${row.studentId}`}
                          value={status}
                          checked={row.status === status}
                          onChange={() => handleStatusChange(row.studentId, status)}
                        />
                        <span>{t(status.toLowerCase())}</span>
                      </label>
                    ))}
                  </div>
                </td>
                <td>{row.firstDetectedAt === 'undetected' ? t('noDetections') : row.firstDetectedAt}</td>
                <td className="notes-tooltip">
                  {formatFirstCheckTimes(row.firstCheckTimes)}
                  <span className="tooltip-text">{formatFirstCheckTimes(row.firstCheckTimes)}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ActionButtons
        onAddNewLecture={() => {}}
        onExport={() => {}}
        onSave={handleSave}
        onDelete={() => {}}
      />
    </div>
  );
};

export default AttendanceTable;