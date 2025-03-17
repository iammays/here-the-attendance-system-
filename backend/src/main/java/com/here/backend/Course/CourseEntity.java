package com.here.backend.Course;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "courses")
public class CourseEntity {
    @Id
    private String courseId;
    private String courseName;
    private String roomId;
    private String teacherId;
    private String studentId;
    private String startTime;
    private String endTime;
    private String day;
    private String category;
    private int Credits;

    public CourseEntity() {}

    public CourseEntity(String courseId, String courseName, String roomId, String teacherId, String startTime, String endTime, String day, String category,int Credits) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.roomId = roomId;
        this.teacherId = teacherId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.category = category;
        this.Credits=Credits;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseid) {
        courseId = courseid;
    }

    public String getName() {
        return courseName;
    }

    public void setName(String courseName) {
        this.courseName = courseName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomid) {
        roomId = roomid;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherid) {
        teacherId = teacherid;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentid) {
        studentId = studentid;
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
    public int getCredit() {
        return Credits;
    }

    public void setCredit(int Credits) {
        this.Credits = Credits;
    }
}