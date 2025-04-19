import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../cssFolder/CourseDashboard.css";
import ScheduleComponent from "./ScheduleComponent.jsx";

const CourseDashboard = () => {
  const { courseName } = useParams();
  const navigate = useNavigate();
  const [courseData, setCourseData] = useState([]);
  const [courseDays, setCourseDays] = useState([]);
  const [weeks, setWeeks] = useState([]);

  const [showModal, setShowModal] = useState(false);
  const [selectedWeek, setSelectedWeek] = useState(null);

  const [upcomingClasses, setUpcomingClasses] = useState([]);
  const [newLecture, setNewLecture] = useState({
    day: "",
    startTime: "",
    endTime: "",
    roomId: "",
  });

  useEffect(() => {
    const fetchCourseData = async () => {
      try {
        const storedUser = JSON.parse(localStorage.getItem("teacher"));
        if (!storedUser) throw new Error("No teacher data found in localStorage");

        const headers = {
          Authorization: `${storedUser.tokenType} ${storedUser.accessToken}`,
        };

        const res = await fetch(`http://localhost:8080/courses/name/${courseName}`, {
          credentials: "include",
          headers,
        });

        if (!res.ok) throw new Error("Failed to fetch course data");

        const data = await res.json();
        setCourseData(data);

        const baseDate = new Date(); // نبدأ من اليوم
        baseDate.setHours(0, 0, 0, 0); // ننظف الوقت
        
        const today = new Date();
        const weeksData = [];
        
        const maxWeekToShow = Math.floor((today - baseDate) / (1000 * 60 * 60 * 24 * 7)) + 1;
        for (let i = 0; i < Math.min(maxWeekToShow, 15); i++) {
          const weekStart = new Date(baseDate);
          weekStart.setDate(baseDate.getDate() + i * 7);
        
          const weekEnd = new Date(weekStart);
          weekEnd.setDate(weekStart.getDate() + 6);
        
          const lecturesThisWeek = data.filter((lecture) => {
            const lectureDate = new Date(lecture.startTime);
            return lectureDate >= weekStart && lectureDate <= weekEnd;
          });
        
          weeksData.push({
            week: i + 1,
            lectures: lecturesThisWeek,
          });
        }
        
        setWeeks(weeksData);
        
      } catch (err) {
        console.error(err.message);
      }
    };

    const fetchUpcoming = async () => {
      try {
        const storedUser = JSON.parse(localStorage.getItem("teacher"));
        const headers = {
          Authorization: `${storedUser.tokenType} ${storedUser.accessToken}`,
        };

        const res = await fetch(
          `http://localhost:8080/teachers/${storedUser.id}/upcoming-classes`,
          { credentials: "include", headers }
        );
        const data = await res.json();

        const sortedClasses = data.sort(
          (a, b) => new Date(a.dateTime) - new Date(b.dateTime)
        );
        setUpcomingClasses(sortedClasses);
      } catch (err) {
        console.error("Error fetching upcoming classes", err);
      }
    };

    const fetchCourseDays = async () => {
      try {
        const storedUser = JSON.parse(localStorage.getItem("teacher"));
        const headers = {
          Authorization: `${storedUser.tokenType} ${storedUser.accessToken}`,
        };
        const res1 = await fetch(`http://localhost:8080/courses/days/${courseName}`, {
          credentials: "include",
          headers,
        });
        if (!res1.ok) throw new Error("Failed to fetch course days");

        const days = await res1.json();
        setCourseDays(days);
      } catch (err) {
        console.error("Error fetching course days", err);
      }
    };

    fetchCourseData();
    fetchUpcoming();
    fetchCourseDays();
  }, [courseName]);

  const handleAddLecture = async (week) => {
    try {
      const storedUser = JSON.parse(localStorage.getItem("teacher"));
      const headers = {
        Authorization: `${storedUser.tokenType} ${storedUser.accessToken}`,
      };

      const lectureData = {
        courseId: courseData[0]?.courseId,
        teacherId: storedUser.id,
        day: newLecture.day,
        startTime: newLecture.startTime,
        endTime: newLecture.endTime,
        roomId: newLecture.roomId,
        week,
      };

      const res = await fetch("http://localhost:8080/courses/manual", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...headers,
        },
        body: JSON.stringify(lectureData),
      });

      if (res.ok) {
        // Re-fetch updated data
        const updatedRes = await fetch(
          `http://localhost:8080/courses/name/${courseName}`,
          { credentials: "include", headers }
        );
        const updatedData = await updatedRes.json();
        setCourseData(updatedData);

        const baseDate = new Date(updatedData[0]?.startTime);
        const today = new Date();
        const updatedWeeks = [];

        for (let i = 0; i < 15; i++) {
          const weekStart = new Date(baseDate);
          weekStart.setDate(baseDate.getDate() + i * 7);

          const weekEnd = new Date(weekStart);
          weekEnd.setDate(weekStart.getDate() + 6);

          if (today >= weekStart) {
            const lecturesThisWeek = updatedData.filter((lecture) => {
              const lectureDate = new Date(lecture.startTime);
              return lectureDate >= weekStart && lectureDate <= weekEnd;
            });

            updatedWeeks.push({
              week: i + 1,
              lectures: lecturesThisWeek,
            });
          }
        }

        setWeeks(updatedWeeks);
        setNewLecture({ day: "", startTime: "", endTime: "", roomId: "" });
      } else {
        console.error("Failed to add lecture");
      }
    } catch (err) {
      console.error(err.message);
    }
  };

  const handleDayClick = (day, week) => {
    navigate(`/schedule/${courseName}/${week}/${day}`);
  };

  if (!courseData.length) return <div className="loading">Loading...</div>;

  return (
    <div className="course-dashboard-container">
      {/* Left Section */}
      <div className="weeks-section">
        <h2 className="course-title">
          {courseData[0]?.name} - {courseData[0]?.courseId}
        </h2>
        {[...weeks].reverse().map((weekData) => (
          <div key={weekData.week} className="week-item">
            <details className="week-details">
              <summary className="week-summary">
                <span className="week-title">Week {weekData.week}</span>
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
              <ul className="days-list">
              {courseDays.map((day, index) => (
                <li
                  key={index}
                  className="day-item"
                  onClick={() => handleDayClick(day, weekData.week)}
                >
                  {day}
                </li>
                ))}
                <button
                  className="add-day-button"
                  onClick={() => {
                  setSelectedWeek(weekData.week);
                  setShowModal(true);
                  }}
                >
                  + Add new day
                </button>
                {showModal && (
                  <ScheduleComponent
                    selectedWeek={selectedWeek}
                    onClose={() => setShowModal(false)}
                  />
                )}
              </ul>
            </details>
          </div>
        ))}
      </div>

      {/* Right Section */}
      <div className="upcoming-section">
        <div className="upcoming-card">
          <h3 className="upcoming-title">Upcoming Classes</h3>
          <ul className="upcoming-list">
            {upcomingClasses.map((cls, index) => (
              <li key={index} className="upcoming-item">
                <div className="course-name">{cls.courseName}</div>
                <div className="room-info">Room: {cls.roomId}</div>
                <div className="time-info">
                  Time:{" "}
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

export default CourseDashboard;
