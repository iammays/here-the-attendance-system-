package com.here.backend.Course;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "courses")
public class CourseEntity {
    @Id
    private String courseId;
    private String lectureId; // تعليق: معرف المحاضرة اليدوية (اختياري)
    private String name;
    private String roomId;
    private String teacherId;
    private String studentId;
    private String startTime; // بصيغة HH:mm
    private String endTime;   // بصيغة HH:mm
    private String day;
    private String category;
    private int Credits;
    private int lateThreshold = 300;

    public CourseEntity() {}

    public CourseEntity(String courseId, String lectureId, String name, String roomId, String teacherId, String startTime, String endTime, String day, String category, int lateThreshold) {
        this.courseId = courseId;
        this.lectureId = lectureId;
        this.name = name;
        this.roomId = roomId;
        this.teacherId = teacherId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.category = category;
        this.lateThreshold = lateThreshold;
    }

    public CourseEntity(String courseId2, String name2, String roomId2, String teacherId2, String startTime2,
            String endTime2, String day2, String category2, int credits, int lateThreshold) {
            this.courseId = courseId2;
            this.name = name2;
            this.roomId = roomId2;
            this.teacherId = teacherId2;
            this.startTime = startTime2;
            this.endTime = endTime2;
            this.day = day2;
            this.category = category2;
            this.Credits = credits;
            this.lateThreshold = lateThreshold != 0 ? lateThreshold : 300;
    }

    public CourseEntity(String courseId2, String name2, String roomId2, String teacherId2, String startTime2,
            String endTime2, String day2, String category2, int credits) {
            this.courseId = courseId2;
            this.name = name2;
            this.roomId = roomId2;
            this.teacherId = teacherId2;
            this.startTime = startTime2;
            this.endTime = endTime2;
            this.day = day2;
            this.category = category2;
            this.Credits = credits;
           
    }

    // Getters and Setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getLectureId() { return lectureId; }
    public void setLectureId(String lectureId) { this.lectureId = lectureId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStartTime() { return startTime; }
    //public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    //public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getCredits() { return Credits; }
    public void setCredits(int credits) { Credits = credits; }
    public int getLateThreshold() { return lateThreshold; }
    public void setLateThreshold(int lateThreshold) { this.lateThreshold = lateThreshold != 0 ? lateThreshold : 300; }


    public void setStartTime(String startTime) {
        if (startTime != null && startTime.matches("\\d{2}:\\d{2}")) {
            this.startTime = startTime;
        } else {
            throw new IllegalArgumentException("Start time must be in HH:mm format");
        }
    }

    public void setEndTime(String endTime) {
        if (endTime != null && endTime.matches("\\d{2}:\\d{2}")) {
            this.endTime = endTime;
        } else {
            throw new IllegalArgumentException("End time must be in HH:mm format");
        }
    }
}