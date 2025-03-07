package com.here.backend.Room;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rooms")
public class RoomEntity {

    @Id
    private String Room_id;
    private String Course_id;

    public RoomEntity() {
    }

    public RoomEntity(String roomId, String courseId) {
        this.Room_id = roomId;
        this.Course_id = courseId;
    }

    public String getRoom_id() {
        return Room_id;
    }

    public void setRoom_id(String room_id) {
        Room_id = room_id;
    }

    public String getCourse_id() {
        return Course_id;
    }

    public void setCourse_id(String course_id) {
        Course_id = course_id;
    }
}