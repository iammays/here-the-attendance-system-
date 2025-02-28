package com.here.backend.Course;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "courses")
public class CourseEntity {
    @Id
    private String CourseId;
    private String name;
    private String RoomId;
    private String TeacherId;
    private String startTime;
    private String endTime;
    private String day;
    private String category;
    
    public CourseEntity() {}

    public CourseEntity(String courseId, String name, String roomId, String teacherId, String startTime, String endTime, String day, String category) {
        this.CourseId = courseId;
        this.name = name;
        this.RoomId = roomId;
        this.TeacherId = teacherId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.category = category;
    }

    public String getCourseId() {
        return CourseId;
    }

    public void setCourseId(String courseid) {
        CourseId = courseid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomId() {
        return RoomId;
    }

    public void setRoomId(String roomid) {
        RoomId = roomid;
    }

    public String getTeacherId() {
        return TeacherId;
    }

    public void setTeacherId(String teacherid) {
        TeacherId = teacherid;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String starttime) {
        this.startTime = starttime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endtime) {
        this.endTime = endtime;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}