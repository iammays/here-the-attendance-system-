package com.here.backend.Attendance;

import java.util.List;

public class AttendanceRecord {
    private String lectureId;
    private String studentId;
    private String courseId;
    private String status;
    private int sessionId; // حقل للجلسة الفردية (اختياري)
    private String detectionTime; // وقت الاكتشاف للجلسة الفردية (اختياري)
    private String screenshotPath;
    private List<AttendanceEntity.SessionAttendance> sessions;
    private List<FirstCheckTime> firstCheckTimes;
    private String firstDetectedAt; // حقل جديد لتخزين أول وقت اكتشاف في المحاضرة

    // كائن داخلي لتمثيل firstCheckTime لكل جلسة
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

    public AttendanceRecord() {}

    public AttendanceRecord(String lectureId, String studentId, String courseId, String status,
                            int sessionId, String detectionTime, String screenshotPath,
                            List<AttendanceEntity.SessionAttendance> sessions,
                            List<FirstCheckTime> firstCheckTimes, String firstDetectedAt) {
        this.lectureId = lectureId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.status = status;
        this.sessionId = sessionId;
        this.detectionTime = detectionTime;
        this.screenshotPath = screenshotPath;
        this.sessions = sessions;
        this.firstCheckTimes = firstCheckTimes;
        this.firstDetectedAt = firstDetectedAt;
    }

    // Getters and Setters
    public String getLectureId() { return lectureId; }
    public void setLectureId(String lectureId) { this.lectureId = lectureId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public String getDetectionTime() { return detectionTime; }
    public void setDetectionTime(String detectionTime) { this.detectionTime = detectionTime; }
    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public List<AttendanceEntity.SessionAttendance> getSessions() { return sessions; }
    public void setSessions(List<AttendanceEntity.SessionAttendance> sessions) { this.sessions = sessions; }
    public List<FirstCheckTime> getFirstCheckTimes() { return firstCheckTimes; }
    public void setFirstCheckTimes(List<FirstCheckTime> firstCheckTimes) { this.firstCheckTimes = firstCheckTimes; }
    public String getFirstDetectedAt() { return firstDetectedAt; }
    public void setFirstDetectedAt(String firstDetectedAt) { this.firstDetectedAt = firstDetectedAt; }
}