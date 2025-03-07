package com.here.backend.Camera;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cameras")
public class CameraEntity {
    private String cameraId;
    private String roomId;

    // Constructor
    public CameraEntity(String cameraId, String roomId) {
        this.cameraId = cameraId;
        this.roomId = roomId;
    }

    // Getters
    public String getCameraId() {
        return cameraId;
    }

    public String getRoomId() {
        return roomId;
    }

    // Setters
    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
