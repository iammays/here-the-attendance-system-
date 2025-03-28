package com.here.backend.Attendance;

public class AttendanceRecord {
    private String lectureId;
    private int sessionId;
    private String studentId;
    private String detectionTime;
    private String screenshotPath;
    private String status;

    // مُنشئ فارغ لإنشاء كائن بدون بيانات
    public AttendanceRecord() {}

    // مُنشئ كامل لإنشاء سجل حضور بكل البيانات
    public AttendanceRecord(String lectureId, int sessionId, String studentId, String detectionTime, String screenshotPath, String status) {
        this.lectureId = lectureId;
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.detectionTime = detectionTime;
        this.screenshotPath = screenshotPath;
        this.status = status;
    }

    public String getLectureId() { return lectureId; }
    public void setLectureId(String lectureId) { this.lectureId = lectureId; }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getDetectionTime() { return detectionTime; }
    public void setDetectionTime(String detectionTime) { this.detectionTime = detectionTime; }
    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}