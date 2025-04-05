package com.here.backend.Schedual;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.here.backend.Course.CourseEntity;

import java.util.List;

@Document(collection = "scheduals")
public class SchedualEntity {

   
    private String scheduleId;  
    private String roomId;      // The room this schedule belongs to
    private List<CourseEntity> listOfSessions; // Nested list of course objects

    // Constructors
    public SchedualEntity() {}

    public SchedualEntity(String scheduleId, String roomId, List<CourseEntity> listOfSessions) {
        this.scheduleId = scheduleId;
        this.roomId = roomId;
        this.listOfSessions = listOfSessions;
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

    public List<CourseEntity> getListOfSessions() {
        return listOfSessions;
    }

    public void setListOfSessions(List<CourseEntity> listOfSessions) {
        this.listOfSessions = listOfSessions;
    }
}