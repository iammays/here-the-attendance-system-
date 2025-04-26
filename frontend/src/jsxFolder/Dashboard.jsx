import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next"; // ✅ Import i18n
import "../cssFolder/Dashboard.css";

const Dashboard = () => {
  const { t } = useTranslation(); // ✅ Setup translation
  const [user, setUser] = useState(null);
  const [courses, setCourses] = useState([]);
  const [upcomingClasses, setUpcomingClasses] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      const storedUser = JSON.parse(localStorage.getItem("teacher"));
      if (!storedUser) {
        console.error("No teacher data found in localStorage");
        return;
      }
      setUser(storedUser);

      const headers = {
        Authorization: `${storedUser.tokenType} ${storedUser.accessToken}`,
      };

      const resCourses = await fetch(
        `http://localhost:8080/teachers/courses/${storedUser.id}`,
        { credentials: "include", headers }
      );
      const coursesData = await resCourses.json();

      const grouped = {};
      coursesData.forEach((course) => {
        if (!grouped[course.category]) grouped[course.category] = [];
        grouped[course.category].push(course.name);
      });
      const groupedCourses = Object.entries(grouped).map(([title, subtopics]) => ({
        title,
        subtopics,
      }));

      const resClasses = await fetch(
        `http://localhost:8080/teachers/${storedUser.id}/upcoming-classes`,
        { credentials: "include", headers }
      );
      const classesData = await resClasses.json();
      const sortedClasses = classesData.sort(
        (a, b) => new Date(a.startTime) - new Date(b.startTime)
      );

      setCourses(groupedCourses);
      setUpcomingClasses(sortedClasses);
    };

    fetchData();
  }, []);

  const handleCourseClick = (courseName) => {
    navigate(`/course/${courseName}`);
  };

  if (!user) return <div className="loading">{t('loading')}</div>; // ✅ Translated Loading

  return (
    <div className="dashboard-container">
      {/* Left Section */}
      <div className="courses-section">
        <h2 className="teacher-name">{t('dr')} {user.name}</h2> {/* ✅ Translated Dr. */}
        {courses.map((subject, index) => (
          <div key={index} className="course-item">
            <details className="course-details">
              <summary className="course-summary">
                <span className="course-title">
                  <span className="course-dot"></span>
                  {subject.title}
                </span>
                <svg
                  className="arrow-icon"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M19 9l-7 7-7-7"
                  ></path>
                </svg>
              </summary>
              <ul className="subtopics-list">
                {subject.subtopics.map((item, i) => (
                  <li
                    key={i}
                    className="subtopic-item"
                    onClick={() => handleCourseClick(item)}
                  >
                    {item}
                  </li>
                ))}
              </ul>
            </details>
          </div>
        ))}
      </div>

      {/* Right Section */}
      <div className="upcoming-section">
        <div className="upcoming-card">
          <h3 className="upcoming-title">{t('upcomingClasses')}</h3> {/* ✅ Translated */}
          <ul className="upcoming-list">
            {upcomingClasses.map((cls, index) => (
              <li key={index} className="upcoming-item">
                <div className="course-name">{cls.courseName}</div>
                <div className="room-info">{t('room')}: {cls.roomId}</div> {/* ✅ */}
                <div className="time-info">
                  {t('time')}: {" "}
                  {new Date(cls.dateTime).toLocaleTimeString([], {
                    hour: "2-digit",
                    minute: "2-digit",
                  })}
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;