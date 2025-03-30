package com.here.backend.Attendance;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "attendances")
public class AttendanceEntity {

    @Id
    private String id;
    private String attendanceId;    // Unique attendance identifier
    private String studentId;        // Student ID
    private String sessionId;        // Session ID
    private String status;           // Attendance status (present, late, excused, absent)
    private LocalDateTime detectedTime; // Time of detection

    // Constructors
    public AttendanceEntity() {}

    public AttendanceEntity(String attendanceId, String studentId, String sessionId, String status, LocalDateTime detectedTime) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.status = status;
        this.detectedTime = detectedTime;
    }

   
   
   
   
   
   
   
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
     // Getters and Setters
    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDetectedTime() {
        return detectedTime;
    }

    public void setDetectedTime(LocalDateTime detectedTime) {
        this.detectedTime = detectedTime;
    }
}
