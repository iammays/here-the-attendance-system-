//backend\src\main\java\com\here\backend\Course\CourseEntity.java

package com.here.backend.Course;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "courses")
public class CourseEntity {
    @Id
    private String courseId;
    private String name;
    private String roomId;
    private String teacherId;
    private String studentId;
    private String startTime;
    private String endTime;
    private String day;
    private String category;

    public CourseEntity() {}

    public CourseEntity(String courseId, String name, String roomId, String teacherId, String startTime, String endTime, String day, String category) {
        this.courseId = courseId;
        this.name = name;
        this.roomId = roomId;
        this.teacherId = teacherId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.category = category;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseid) {
        courseId = courseid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}