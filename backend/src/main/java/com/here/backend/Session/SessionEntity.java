package com.here.backend.Session;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sessions")
public class SessionEntity {

    @Id
    private String sessionId;  // Unique session identifier
    private String courseId;    // Course ID
    private String roomId;      // Room ID
    private LocalDateTime startTime;  // Session start time
    private LocalDateTime endTime;    // Session end time
    private String status;      // Status (ongoing, completed, scheduled)
    private String scheduleId;
    // Constructors
    public SessionEntity() {}

    public SessionEntity(String sessionId, String courseId, String roomId, LocalDateTime startTime, LocalDateTime endTime, String status,String scheduleId) {
        this.sessionId = sessionId;
        this.courseId = courseId;
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.scheduleId=scheduleId;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }
}
