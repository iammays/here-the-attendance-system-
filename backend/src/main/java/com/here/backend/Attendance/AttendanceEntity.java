// backend/src/main/java/com/here/backend/Attendance/AttendanceEntity.java
package com.here.backend.Attendance;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "attendances")
public class AttendanceEntity {
    @Id
    private String attendanceId;
    private String lectureId;   // معرف المحاضرة (مثل CS101-2025-04-05)
    private String courseId;    // معرف الكورس الأساسي (مثل CS101)
    private String studentId;   // رقم الطالب
    private String studentName; // اسم الطالب
    private String status;      // الحالة: Present, Late, Absent, Excuse
    private List<SessionAttendance> sessions;

    public static class SessionAttendance {
        private int sessionId;
        private String firstDetectionTime; // وقت أول اكتشاف في الجلسة (أو "undetected")

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
}