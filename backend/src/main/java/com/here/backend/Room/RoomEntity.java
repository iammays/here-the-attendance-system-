//backend\src\main\java\com\here\backend\Room\RoomEntity.java

package com.here.backend.Room;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "rooms")
public class RoomEntity {

    @Field("room_id")
    private String Room_id;
    private String scheduleId;

    public RoomEntity() {
    }

    public RoomEntity(String roomId, String courseId) {
        this.Room_id = roomId;
        this.scheduleId = courseId;
    }

    public String getRoom_id() {
        return Room_id;
    }

    public void setRoom_id(String room_id) {
        Room_id = room_id;
    }

    public String getCourse_id() {
        return scheduleId;
    }

    public void setCourse_id(String course_id) {
        scheduleId = course_id;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }
}