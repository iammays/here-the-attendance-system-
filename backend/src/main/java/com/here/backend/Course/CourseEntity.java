package com.here.backend.Course;

import java.util.List;

public class CourseEntity {
    private String courseId;
    private String courseName;
    private String teacherId;
    private List<String> roomIds;

    // Constructor
    public CourseEntity(String courseId, String courseName, String teacherId, List<String> roomIds) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacherId = teacherId;
        this.roomIds = roomIds;
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public List<String> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(List<String> roomIds) {
        this.roomIds = roomIds;
    }
}
