package com.here.backend.Room;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document(collection = "rooms")
public class RoomEntity {

    @Id
    private String roomId;
    private String courseId;

    public RoomEntity() {
    }

    public RoomEntity(String roomId, String courseId) {
        this.roomId = roomId;
        this.courseId = courseId;
    }

    public String getRoom_id() {
        return roomId;
    }

    public void setRoom_id(String roomId) {
        this.roomId = roomId;
    }

    public String getCourse_id() {
        return courseId;
    }

    public void setCourse_id(String courseId) {
        this.courseId = courseId;
    }

    public String getScheduleId() {
        return courseId;
    }

    public void setScheduleId(String courseId) {
        this.courseId = courseId;
    }
}