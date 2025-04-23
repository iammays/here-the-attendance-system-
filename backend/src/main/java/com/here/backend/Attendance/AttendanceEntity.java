package com.here.backend.Attendance;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "attendances")
public class AttendanceEntity {
    @Id
    private String attendanceId;
    private String lectureId;
    private String courseId;
    private String studentId;
    private String studentName;
    private String status;
    private List<SessionAttendance> sessions;
    private List<FirstCheckTime> firstCheckTimes;
    private String firstDetectedAt; // حقل جديد لتخزين أول وقت اكتشاف في المحاضرة

    public static class SessionAttendance {
        private int sessionId;
        private String firstDetectionTime;

        public SessionAttendance() {}

        public SessionAttendance(int sessionId, String firstDetectionTime) {
            this.sessionId = sessionId;
            this.firstDetectionTime = firstDetectionTime;
        }

        public int getSessionId() { return sessionId; }
        public void setSessionId(int sessionId) { this.sessionId = sessionId; }
        public String getFirstDetectionTime() { return firstDetectionTime; }
        public void setFirstDetectionTime(String firstDetectionTime) { this.firstDetectionTime = firstDetectionTime; }
    }

    public static class FirstCheckTime {
        private int sessionId;
        private String firstCheckTime;

        public FirstCheckTime() {}

        public FirstCheckTime(int sessionId, String firstCheckTime) {
            this.sessionId = sessionId;
            this.firstCheckTime = firstCheckTime;
        }

        public int getSessionId() { return sessionId; }
        public void setSessionId(int sessionId) { this.sessionId = sessionId; }
        public String getFirstCheckTime() { return firstCheckTime; }
        public void setFirstCheckTime(String firstCheckTime) { this.firstCheckTime = firstCheckTime; }
    }

    // Getters and Setters
    public String getAttendanceId() { return attendanceId; }
    public void setAttendanceId(String attendanceId) { this.attendanceId = attendanceId; }
    public String getLectureId() { return lectureId; }
    public void setLectureId(String lectureId) { this.lectureId = lectureId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<SessionAttendance> getSessions() { return sessions; }
    public void setSessions(List<SessionAttendance> sessions) { this.sessions = sessions; }
    public List<FirstCheckTime> getFirstCheckTimes() { return firstCheckTimes; }
    public void setFirstCheckTimes(List<FirstCheckTime> firstCheckTimes) { this.firstCheckTimes = firstCheckTimes; }
    public String getFirstDetectedAt() { return firstDetectedAt; }
    public void setFirstDetectedAt(String firstDetectedAt) { this.firstDetectedAt = firstDetectedAt; }
}