package com.here.backend.Room;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "rooms")
public class RoomEntity {

    @Id
    private String roomId;
    private List<String> courseIds;

    public RoomEntity() {
    }

    public RoomEntity(String roomId, List<String> courseIds) {
        this.roomId = roomId;
        this.courseIds = courseIds;
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
}
