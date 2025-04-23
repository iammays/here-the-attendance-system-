import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import SearchBar from './SearchBar';
import LectureNavigation from './LectureNavigation';
import ActionButtons from './ActionButtons';
import '../cssFolder/AttendanceTable.css';

const AttendanceTable = () => {
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

  const getAuthHeaders = () => {
    const teacher = JSON.parse(localStorage.getItem('teacher'));
    if (!teacher?.accessToken) {
      console.error('No access token found in localStorage');
      setError('يرجى تسجيل الدخول لإتمام العملية');
      return {};
    }
    return { Authorization: `Bearer ${teacher.accessToken}` };
  };

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
        setError(`خطأ في جلب البيانات: ${err.response?.status === 401 ? 'غير مصرح: يرجى تسجيل الدخول' : err.message}`);
        console.error('Fetch error:', err);
      }
    };
    fetchData();
  }, [courseId, lectureId]);

  const handleStatusChange = (studentId, newStatus) => {
    setStatusChanges(prev => ({
      ...prev,
      [studentId]: newStatus,
    }));

    setFilteredData(prev =>
      prev.map(row =>
        row.studentId === studentId ? { ...row, status: newStatus } : row
      )
    );
  };

  const handleSave = async () => {
    try {
      const updatePromises = Object.entries(statusChanges).map(([studentId, newStatus]) =>
        axios.put(
          `http://localhost:8080/api/attendances/updateWithEmail/${lectureId}/${studentId}`,
          { status: newStatus },
          { headers: getAuthHeaders() }
        )
      );

      await Promise.all(updatePromises);

      setAttendanceData(prev =>
        prev.map(row => ({
          ...row,
          status: statusChanges[row.studentId] || row.status,
        }))
      );

      setStatusChanges({});
      alert('تم حفظ التغييرات بنجاح');
    } catch (err) {
      setError(`خطأ أثناء حفظ التغييرات: ${err.response?.data || err.message}`);
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

  const handleSearch = (searchTerm) => {
    const filtered = attendanceData.filter(
      row =>
        row.studentId.toLowerCase().includes(searchTerm.toLowerCase()) ||
        row.studentName.toLowerCase().includes(searchTerm.toLowerCase())
    );
    setFilteredData(filtered);
  };

  const handleFilter = (status) => {
    if (status === 'All') {
      setFilteredData(attendanceData);
    } else {
      const filtered = attendanceData.filter(row => row.status === status);
      setFilteredData(filtered);
    }
  };

  const handleNavigateLecture = (direction) => {
    const newIndex = direction === 'next' ? currentLectureIndex + 1 : currentLectureIndex - 1;
    if (newIndex >= 0 && newIndex < lectures.length) {
      const newLectureId = lectures[newIndex].lectureId;
      setCurrentLectureIndex(newIndex);
      navigate(`/attendance/${courseId}/${newLectureId}`);
    }
  };

  const handleAddNewLecture = async () => {
    try {
      const newLecture = {
        courseId,
        date: new Date().toISOString().split('T')[0],
        startTime: '09:00',
        endTime: '10:30',
        day: 'Monday',
        name: lectureInfo.courseName || 'New Lecture',
        teacherId: 'T001',
        category: 'General',
        credits: 3,
      };
      const response = await axios.post(`http://localhost:8080/courses/manual`, newLecture, {
        headers: getAuthHeaders(),
      });
      const newLectureId = response.data.lectureId;
      setLectures([...lectures, response.data]);
      alert(`تمت إضافة محاضرة جديدة: ${newLectureId}`);
    } catch (err) {
      setError(`خطأ أثناء إضافة المحاضرة: ${err.response?.data || err.message}`);
      console.error('Add lecture error:', err);
    }
  };

  const handleExport = async () => {
    try {
      const exportData = filteredData.map(row => ({
        name: row.studentName,
        status: row.status,
        firstDetectedAt: row.firstDetectedAt,
        sessions: row.firstCheckTimes.map(check => `Session ${check.sessionId}: ${check.firstCheckTime}`).join(', '),
      }));
      await axios.post(`http://localhost:8080/api/excel/export-attendance`, exportData, {
        headers: getAuthHeaders(),
      });
      alert('تم تصدير الحضور إلى Excel');
    } catch (err) {
      setError(`خطأ أثناء التصدير إلى Excel: ${err.response?.data || err.message}`);
      console.error('Export error:', err);
    }
  };

  const handleDelete = async () => {
    try {
      console.log(`Sending DELETE request for lectureId: ${lectureId}`);
      const response = await axios.delete(`http://localhost:8080/api/attendances/deleteTempLecture/${lectureId}`, {
        headers: getAuthHeaders(),
      });
      console.log('Delete response:', response.data);
      alert('تم حذف المحاضرة بنجاح');
      navigate(`/course/${courseId}`);
    } catch (err) {
      const errorMessage = err.response?.data || err.message;
      setError(`خطأ أثناء حذف المحاضرة: ${errorMessage}`);
      console.error('Delete error:', err);
    }
  };

  const formatFirstCheckTimes = (firstCheckTimes) => {
    if (!firstCheckTimes || firstCheckTimes.length === 0) {
      return 'No detections';
    }
    const validCheckTimes = firstCheckTimes
      .filter(check => check.firstCheckTime !== 'undetected')
      .map(check => `Session ${check.sessionId}: ${check.firstCheckTime}`);
    return validCheckTimes.length > 0 ? validCheckTimes.join(', ') : 'No detections';
  };

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div className="attendance-container">
      <div className="content">
        <div className="course-info">
          <h2>{lectureInfo.courseName}</h2>
          <p>اليوم: {lectureInfo.day}</p>
          <p>وقت البدء: {lectureInfo.startTime}</p>
        </div>
        <LectureNavigation
          lectureInfo={lectureInfo}
          currentLectureIndex={currentLectureIndex}
          totalLectures={lectures.length}
          onNavigate={handleNavigateLecture}
        />
        <SearchBar onSearch={handleSearch} onFilter={handleFilter} />
        <table className="attendance-table">
          <thead>
            <tr>
              <th onClick={() => handleSort('studentName')}>اسم الطالب</th>
              <th onClick={() => handleSort('status')}>الحالة</th>
              <th onClick={() => handleSort('firstDetectedAt')}>وقت التسجيل الأول</th>
              <th onClick={() => handleSort('detectionCount')}>ملاحظات إضافية</th>
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
                        <span>{status.charAt(0)}</span>
                      </label>
                    ))}
                  </div>
                </td>
                <td>{row.firstDetectedAt === 'undetected' ? 'غير مكتشف' : row.firstDetectedAt}</td>
                <td className="notes-tooltip">
                  {formatFirstCheckTimes(row.firstCheckTimes)}
                  <span className="tooltip-text">{formatFirstCheckTimes(row.firstCheckTimes)}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <ActionButtons
          onAddNewLecture={handleAddNewLecture}
          onExport={handleExport}
          onSave={handleSave}
          onDelete={handleDelete}
        />
      </div>
    </div>
  );
};

export default AttendanceTable;