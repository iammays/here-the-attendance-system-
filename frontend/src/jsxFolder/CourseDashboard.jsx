import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import "../cssFolder/CourseDashboard.css";

const CourseDashboard = () => {
  const { t } = useTranslation();
  const { courseName } = useParams();
  const navigate = useNavigate();
  const [courseData, setCourseData] = useState(null);
  const [lectures, setLectures] = useState([]);
  const [courseDays, setCourseDays] = useState([]);
  const [weeks, setWeeks] = useState([]);
  const [upcomingClasses, setUpcomingClasses] = useState([]);
  const [courseId, setCourseId] = useState(null);
  const [lateThreshold, setLateThreshold] = useState("5");
  const [showLateModal, setShowLateModal] = useState(false);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const getAuthHeaders = () => {
    const teacher = JSON.parse(localStorage.getItem("teacher"));
    if (!teacher?.accessToken) {
      setError(t("loginRequired"));
      navigate("/login");
      return {};
    }
    return {
      Authorization: `${teacher.tokenType || "Bearer"} ${teacher.accessToken}`,
      "Content-Type": "application/json",
    };
  };

  useEffect(() => {
    const fetchCourseData = async () => {
      try {
        setLoading(true);
        setError(null);
        const headers = getAuthHeaders();
        if (!headers.Authorization) return;

        // Fetch course template
        const courseResponse = await fetch(`http://localhost:8080/courses/name/${courseName}`, {
          headers,
          credentials: "include",
        });
        if (!courseResponse.ok) {
          const errorText = await courseResponse.text();
          throw new Error(`Failed to fetch course data: ${courseResponse.status} ${errorText}`);
        }
        const courses = await courseResponse.json();
        if (!courses.length) {
          throw new Error(t("noData"));
        }
        const courseTemplate = courses.find(c => !c.lectureId);
        if (!courseTemplate) {
          throw new Error(t("noCourseTemplate"));
        }
        setCourseData(courseTemplate);
        setCourseId(courseTemplate.courseId);

        // Fetch course days
        const daysResponse = await fetch(`http://localhost:8080/courses/days/${courseName}`, {
          headers,
          credentials: "include",
        });
        if (!daysResponse.ok) {
          const errorText = await daysResponse.text();
          throw new Error(`Failed to fetch course days: ${daysResponse.status} ${errorText}`);
        }
        const daysData = await daysResponse.json();
        setCourseDays(daysData);
        console.log("Course days:", daysData);

        // Fetch all lectures for the course
        const lecturesResponse = await fetch(`http://localhost:8080/courses/${courseTemplate.courseId}/lectures`, {
          headers,
          credentials: "include",
        });
        if (!lecturesResponse.ok) {
          const errorText = await lecturesResponse.text();
          throw new Error(`Failed to fetch lectures: ${lecturesResponse.status} ${errorText}`);
        }
        const lecturesData = await lecturesResponse.json();
        setLectures(lecturesData);

        // Generate weeks
        const startFromDate = new Date("2025-01-28");
        startFromDate.setHours(0, 0, 0, 0);
        const today = new Date();
        const weeksData = [];
        const maxWeekToShow = Math.floor((today - startFromDate) / (1000 * 60 * 60 * 24 * 7)) + 1;

        for (let i = 0; i < Math.min(maxWeekToShow, 15); i++) {
          const weekStart = new Date(startFromDate);
          weekStart.setDate(startFromDate.getDate() + i * 7);
          const weekEnd = new Date(weekStart);
          weekEnd.setDate(weekStart.getDate() + 6);

          const lecturesThisWeek = lecturesData.filter((lecture) => {
            const lectureDate = lecture.lectureId.split('-')[1];
            const date = new Date(lectureDate);
            return date >= weekStart && date <= weekEnd;
          });

          weeksData.push({
            week: i + 1,
            lectures: lecturesThisWeek,
          });
        }
        setWeeks(weeksData);
      } catch (err) {
        const errorMessage = err.message.includes("401") ? t('unauthorized') :
                            err.message.includes("404") ? t('noData') :
                            err.message.includes("500") ? t('serverError') :
                            err.message;
        setError(`${t("fetchError")}: ${errorMessage}`);
        console.error("Error fetching course data:", err);
      } finally {
        setLoading(false);
      }
    };

    const fetchUpcoming = async () => {
      try {
        const teacher = JSON.parse(localStorage.getItem("teacher"));
        if (!teacher?.id) {
          console.warn("No teacher ID found in localStorage");
          return;
        }
        const headers = getAuthHeaders();
        const response = await fetch(`http://localhost:8080/teachers/${teacher.id}/upcoming-classes`, {
          headers,
          credentials: "include",
        });
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`Failed to fetch upcoming classes: ${response.status} ${errorText}`);
        }
        const data = await response.json();
        const sortedClasses = data.sort((a, b) => new Date(a.dateTime) - new Date(b.dateTime));
        setUpcomingClasses(sortedClasses);
      } catch (err) {
        console.error("Error fetching upcoming classes:", err);
      }
    };

    fetchCourseData();
    fetchUpcoming();
  }, [courseName, t, navigate]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (!e.target.closest(".late-popup") && !e.target.closest(".change-late-btn")) {
        setShowLateModal(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLateThresholdSave = async () => {
    try {
      setLoading(true);
      setError(null);
      if (!courseId) {
        setError(t("noCourseId"));
        return;
      }
      if (!lateThreshold || isNaN(lateThreshold) || Number(lateThreshold) < 0) {
        setError(t("invalidLateThreshold"));
        return;
      }

      const headers = getAuthHeaders();
      const thresholdInSeconds = parseInt(lateThreshold) * 60;
      const response = await fetch(`http://localhost:8080/courses/${courseId}/lateThreshold`, {
        method: "PUT",
        headers,
        credentials: "include",
        body: JSON.stringify({ lateThreshold: thresholdInSeconds }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Failed to update late threshold: ${response.status} ${errorText}`);
      }

      setShowLateModal(false);
      alert(t("lateThresholdUpdated"));
    } catch (err) {
      setError(`${t("lateThresholdError")}: ${err.message}`);
      console.error("Error updating late threshold:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleDayClick = async (day, week) => {
    try {
      setError(null);
      // Calculate the lecture date
      const semesterStart = new Date("2025-01-28"); // Tuesday
      semesterStart.setHours(0, 0, 0, 0);

      // Define days of week aligned with Date.getDay() (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
      const daysOfWeek = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];
      const targetDayIndex = daysOfWeek.indexOf(day.toUpperCase());
      if (targetDayIndex === -1) {
        throw new Error(`Invalid day: ${day}`);
      }

      // Get the day of the week for the semester start (Tuesday = 2)
      const startDayIndex = semesterStart.getDay();

      // Calculate days to target day in the first week
      let daysToTargetDay;
      if (targetDayIndex === 1) { // MONDAY
        daysToTargetDay = 7; // Always 7 days from Tuesday to next Monday
      } else {
        daysToTargetDay = targetDayIndex < startDayIndex 
          ? 7 - (startDayIndex - targetDayIndex) 
          : (targetDayIndex - startDayIndex) + 1;
      }
      const daysToAdd = (week - 1) * 7 + daysToTargetDay;

      // Calculate the target date in UTC to avoid timezone issues
      const lectureDate = new Date(Date.UTC(
        semesterStart.getUTCFullYear(),
        semesterStart.getUTCMonth(),
        semesterStart.getUTCDate() + daysToAdd
      ));
      const formattedDate = lectureDate.toISOString().split('T')[0]; // YYYY-MM-DD
      const calculatedDay = daysOfWeek[lectureDate.getDay()];

      // Log for debugging
      console.log(`Clicked day: ${day}, Week: ${week}, Days to add: ${daysToAdd}, Target day index: ${targetDayIndex}, Start day index: ${startDayIndex}, Days to target: ${daysToTargetDay}, Calculated date: ${formattedDate}, Calculated day: ${calculatedDay}`);

      // Validate that the calculated day matches the clicked day
      if (calculatedDay.toUpperCase() !== day.toUpperCase()) {
        throw new Error(`Date calculation error: Expected ${day}, got ${calculatedDay} for date ${formattedDate}`);
      }

      // Validate courseData and time formats
      if (!courseData || !courseData.startTime || !courseData.endTime) {
        throw new Error(t("missingCourseData"));
      }
      let startTime = courseData.startTime;
      let endTime = courseData.endTime;
      const timeRegex = /^\d{2}:\d{2}$/;
      if (!timeRegex.test(startTime) || !timeRegex.test(endTime)) {
        console.warn(`Invalid time format - startTime: ${startTime}, endTime: ${endTime}`);
        startTime = "15:00"; // Default to match observed time
        endTime = "16:00";   // Default to match observed time
      }

      const formattedStartTime = startTime.replace(":", "");
      const lectureId = `${courseId}-${formattedDate}-${formattedStartTime}`;

      console.log(`Attempting to navigate to lecture: ${lectureId}`);

      // Check if lecture exists
      const existingLecture = lectures.find(l => l.lectureId === lectureId);
      if (existingLecture) {
        console.log(`Lecture already exists: ${lectureId}`);
        navigate(`/attendance/${courseId}/${lectureId}`);
        return;
      }

      // Create new lecture
      const headers = getAuthHeaders();
      const lectureData = {
        courseId,
        date: formattedDate,
        startTime,
        endTime,
        day: calculatedDay,
        roomId: courseData?.roomId || "B-205",
      };
      console.log("Lecture data to be sent:", JSON.stringify(lectureData, null, 2));

      const response = await fetch(`http://localhost:8080/api/attendances/addNewLecture`, {
        method: "POST",
        headers,
        credentials: "include",
        body: JSON.stringify(lectureData),
      });

      if (!response.ok) {
        const errorBody = await response.text();
        let errorMessage;
        try {
          const errorJson = JSON.parse(errorBody);
          errorMessage = errorJson.error || errorBody;
        } catch {
          errorMessage = errorBody || "Unknown error";
        }
        throw new Error(`Failed to create lecture: ${response.status} ${errorMessage}`);
      }

      const responseData = await response.json();
      const newLectureId = responseData.lectureId;
      console.log(`Lecture created successfully: ${newLectureId}`);

      // Update lectures state
      const newLecture = {
        courseId,
        lectureId: newLectureId,
        name: courseData.name,
        roomId: courseData?.roomId || "B-205",
        teacherId: courseData.teacherId,
        startTime,
        endTime,
        day: calculatedDay,
        category: courseData.category,
        credits: courseData.credits,
        lateThreshold: courseData.lateThreshold,
      };
      setLectures(prev => [...prev, newLecture]);

      navigate(`/attendance/${courseId}/${newLectureId}`);
    } catch (err) {
      setError(`${t("navigationError")}: ${err.message}`);
      console.error("Error navigating to attendance:", err);
    }
  };

  if (loading) return <div className="loading">{t("loading")}</div>;
  if (error) return <div className="error">{error}</div>;
  if (!courseData) return <div className="loading">{t("noData")}</div>;

  return (
    <div className="course-dashboard-container">
      <div className="weeks-section">
        <h2 className="course-title">
          <span className="course-info">
            {courseData.name} - {courseData.courseId}
          </span>
          <div className="late-container">
            <button
              className="change-late-btn"
              onClick={() => setShowLateModal(true)}
              disabled={loading}
            >
              {t("changeLateTime")}
            </button>
            {showLateModal && (
              <div className="late-popup">
                <h3>{t("editLateTime")}</h3>
                {error && <div className="error">{error}</div>}
                <label htmlFor="lateThreshold">{t("lateAfter")}:</label>
                <input
                  type="number"
                  id="lateThreshold"
                  name="lateThreshold"
                  value={lateThreshold}
                  onChange={(e) => setLateThreshold(e.target.value)}
                  min="0"
                />
                <span>{t("minutes")}</span>
                <div>
                  <button onClick={handleLateThresholdSave} disabled={loading}>
                    {loading ? t("saving") : t("save")}
                  </button>
                  <button
                    onClick={() => setShowLateModal(false)}
                    disabled={loading}
                  >
                    {t("cancel")}
                  </button>
                </div>
              </div>
            )}
          </div>
        </h2>

        {[...weeks].reverse().map((weekData) => (
          <div key={weekData.week} className="week-item">
            <details className="week-details">
              <summary className="week-summary">
                <span className="week-title">{t("week")} {weekData.week}</span>
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
              <ul 
                className="days-list" 
                style={{ 
                  fontWeight: 500,
                  fontSize: '20px',
                  color: '#2A4B70',
                  marginBottom: '5px'
                }}
              >
                {courseDays.map((day, index) => (
                  <li
                    key={index}
                    className="day-item"
                    onClick={() => handleDayClick(day, weekData.week)}
                  >
                    {t(day)}
                    {weekData.lectures.some(l => l.day === day) ? (
                      <span className="lecture-status">{t("")}</span>
                    ) : (
                      <span className="lecture-status">{t("")}</span>
                    )}
                  </li>
                ))}
              </ul>
            </details>
          </div>
        ))}
      </div>

      <div className="upcoming-section">
        <div className="upcoming-card">
          <h3 className="upcoming-title">{t("upcomingClasses")}</h3>
          {upcomingClasses.length === 0 ? (
            <p>{t("noUpcomingClasses")}</p>
          ) : (
            <ul className="upcoming-list">
              {upcomingClasses.map((cls, index) => (
                <li key={index} className="upcoming-item">
                  <div className="course-name">{t(cls.courseName)}</div>
                  <div className="room-info">
                    {t("room")}: {cls.roomId || t("noRoom")}
                  </div>
                  <div className="time-info">
                    {t("time")}:{" "}
                    {new Date(cls.dateTime).toLocaleTimeString([], {
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
};

export default CourseDashboard;