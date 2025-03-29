//backend\src\main\java\com\here\backend\Attendance\AttendanceEntity.java

package com.here.backend.Attendance;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "attendances")
public class AttendanceEntity {

    @Id
    private String id;
    private String lectureId;
    private String studentId;
    private String status;  // حقل جديد
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
    public String getLectureId() { return lectureId; }
    public void setLectureId(String lectureId) { this.lectureId = lectureId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<SessionAttendance> getSessions() { return sessions; }
    public void setSessions(List<SessionAttendance> sessions) { this.sessions = sessions; }
}