import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../cssFolder/CourseDashboard.css";

const CourseDashboard = () => {
  const { courseName } = useParams();
  const navigate = useNavigate();
  const [courseData, setCourseData] = useState([]);
  const [courseDays, setCourseDays] = useState([]);
  const [weeks, setWeeks] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [selectedWeek, setSelectedWeek] = useState(null);
  const [upcomingClasses, setUpcomingClasses] = useState([]);
  const [courseId, setCourseId] = useState(null);

  const [lateThreshold, setLateThreshold] = useState("5"); // وقت التأخير بالدقائق
  const [showLateModal, setShowLateModal] = useState(false); // مودال ضبط وقت التأخير

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
        setCourseId(data[0]?.courseId);

        // حساب الأسابيع بدءًا من تاريخ مُعين (يمكن تعديل التاريخ حسب الحاجة)
        const startFromDate = new Date("2025-01-02");
        startFromDate.setHours(0, 0, 0, 0);

        const today = new Date();
        const weeksData = [];

        // نحسب عدد الأسابيع التي مرّت من startFromDate حتى اليوم الحالي
        const maxWeekToShow = Math.floor((today - startFromDate) / (1000 * 60 * 60 * 24 * 7)) + 1;

        // نعرض حد أقصى 15 أسبوع
        for (let i = 0; i < Math.min(maxWeekToShow, 15); i++) {
          const weekStart = new Date(startFromDate);
          weekStart.setDate(startFromDate.getDate() + i * 7);

          const weekEnd = new Date(weekStart);
          weekEnd.setDate(weekStart.getDate() + 6);

          // هنا نقوم بتصفية المحاضرات التي تقع ضمن هذا الأسبوع
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

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (!e.target.closest(".late-popup") && !e.target.closest(".late-icon")) {
        setShowLateModal(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLateThresholdSave = async () => {
    try {
      const storedUser = JSON.parse(localStorage.getItem("teacher"));
      const headers = {
        "Content-Type": "application/json",
        Authorization: `${storedUser.tokenType} ${storedUser.accessToken}`,
      };

      // نحول الدقائق إلى ثواني لأن الباك يستخدم الثواني
      const thresholdInSeconds = parseInt(lateThreshold) * 60;

      const res = await fetch(`http://localhost:8080/courses/${courseId}/lateThreshold`, {
        method: "PUT",
        headers,
        credentials: "include",
        body: JSON.stringify({ lateThreshold: thresholdInSeconds }),
      });

      if (!res.ok) throw new Error("Failed to update late threshold");

      alert("تم تحديث وقت التأخير بنجاح ✅");
      setShowLateModal(false);
    } catch (err) {
      console.error("Error updating late threshold:", err.message);
      alert("فشل التحديث ❌");
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

      {/* مودال ضبط وقت التأخير */}
      <span
        className="late-icon"
        onClick={() => setShowLateModal(true)}
        title="Set Late Threshold"
        style={{
          marginLeft: "10px",
          cursor: "pointer",
          fontSize: "20px",
          color: "#555",
          position: "relative",
        }}
      >
        ⏰
      </span>

      {showLateModal && (
        <div
          className="late-popup"
          style={{
            position: "absolute",
            top: "100px",
            right: "50px", // عدل حسب المكان المطلوب
            background: "white",
            border: "1px solid #ccc",
            borderRadius: "8px",
            padding: "10px",
            zIndex: 1000,
            boxShadow: "0px 2px 10px rgba(0,0,0,0.2)",
            minWidth: "160px",
          }}
        >
          <label style={{ fontSize: "14px" }}>late after:</label>
          <input
            type="number"
            value={lateThreshold}
            onChange={(e) => setLateThreshold(e.target.value)}
            min="0"
            style={{
              width: "50px",
              padding: "4px",
              marginLeft: "5px",
              fontSize: "14px",
            }}
          />
          <span style={{ fontSize: "14px", marginLeft: "4px" }}>minutes</span>
          <div style={{ marginTop: "8px", textAlign: "right" }}>
            <button
              onClick={handleLateThresholdSave}
              style={{
                padding: "4px 8px",
                fontSize: "12px",
                marginRight: "5px",
              }}
            >
              save
            </button>
            <button
              onClick={() => setShowLateModal(false)}
              style={{
                padding: "4px 8px",
                fontSize: "12px",
              }}
            >
              cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default CourseDashboard;

