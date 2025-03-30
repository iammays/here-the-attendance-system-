//backend\src\main\java\com\here\backend\Attendance\AttendanceEntity.java

package com.here.backend.Attendance;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "attendances")
public class AttendanceEntity {

    @Id
    private String id;

    private String attendanceId;    // Unique attendance identifier
    private String studentId;        // Student ID
    private String sessionId; 
    private String lectureId;
        private String status;           // Attendance status (present, late, excused, absent)
    private LocalDateTime detectedTime; // Time of detection

    private List<SessionAttendance> sessions;


    // كلاس صغير لتخزين رقم الجلسة ووقت الاكتشاف
    public static class SessionAttendance {
        private int sessionId;
        private String detectionTime;

        public SessionAttendance(int sessionId, String detectionTime) {
            this.sessionId = sessionId;
            this.detectionTime = detectionTime;
        }

        public int getSessionId() { return sessionId; }
        public void setSessionId(int sessionId) { this.sessionId = sessionId; }
        public String getDetectionTime() { return detectionTime; }
        public void setDetectionTime(String detectionTime) { this.detectionTime = detectionTime; }
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

    public String getsessionId() { return sessionId; }
    public void setsessionId(String sessionId) { this.sessionId = sessionId; }
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

    public String getLectureId() { return lectureId; }
    public void setLectureId(String lectureId) { this.lectureId = lectureId; }
    public List<SessionAttendance> getSessions() { return sessions; }
    public void setSessions(List<SessionAttendance> sessions) { this.sessions = sessions; }
}
