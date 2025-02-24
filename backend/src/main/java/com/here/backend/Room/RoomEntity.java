package com.here.backend.Room;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "rooms")
public class RoomEntity {

    @Id
    private String roomId;
    private List<String> courseIds;
    private String scheduleId; // The schedule assigned to this room

    public RoomEntity() {
    }

    public RoomEntity(String roomId, List<String> courseIds, String scheduleId) {
        this.roomId = roomId;
        this.courseIds = courseIds;
        this.scheduleId = scheduleId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public List<String> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<String> courseIds) {
        this.courseIds = courseIds;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }
}
