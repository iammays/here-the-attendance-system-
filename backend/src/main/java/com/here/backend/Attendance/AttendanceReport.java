// backend/src/main/java/com/here/backend/Attendance/AttendanceReport.java
package com.here.backend.Attendance;

import java.util.List;

public class AttendanceReport {
    private String status;
    private int detectionCount;
    private List<AttendanceEntity.SessionAttendance> sessions;

    // Default constructor
    public AttendanceReport() {}

    // Parameterized constructor
    public AttendanceReport(String status, int detectionCount, List<AttendanceEntity.SessionAttendance> sessions) {
        this.status = status;
        this.detectionCount = detectionCount;
        this.sessions = sessions;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getDetectionCount() { return detectionCount; }
    public void setDetectionCount(int detectionCount) { this.detectionCount = detectionCount; }
    public List<AttendanceEntity.SessionAttendance> getSessions() { return sessions; }
    public void setSessions(List<AttendanceEntity.SessionAttendance> sessions) { this.sessions = sessions; }
}