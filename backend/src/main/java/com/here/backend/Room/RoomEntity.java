package com.here.backend.Room;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "rooms")
public class RoomEntity {

    @Id
    private String roomId;
    private String scheduleId; // The schedule assigned to this room

    public RoomEntity() {
    }

    public RoomEntity(String roomId,  String scheduleId) {
        this.roomId = roomId;
        this.scheduleId = scheduleId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }


    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }
}
