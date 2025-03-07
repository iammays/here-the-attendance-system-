package com.here.backend.Schedual;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "scheduals")
public class SchedualEntity {

    @Id
    private String scheduleId;  // Unique schedule identifier
    private String roomId;      // The room this schedule belongs to
    private List<String> sessionIds; // List of session IDs in this schedule

    // Constructors
    public SchedualEntity() {}

    public SchedualEntity(String scheduleId, String roomId, List<String> sessionIds) {
        this.scheduleId = scheduleId;
        this.roomId = roomId;
        this.sessionIds = sessionIds;
    }



    public SchedualEntity(String scheduleId, String roomId) {
        this.scheduleId = scheduleId;
        this.roomId = roomId;
    }
    // Getters and Setters
    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public List<String> getSessionIds() {
        return sessionIds;
    }

    public void setSessionIds(List<String> sessionIds) {
        this.sessionIds = sessionIds;
    }
}
