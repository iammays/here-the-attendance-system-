package com.here.backend.Lecture;

import java.time.DayOfWeek;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lectures")
public class LectureEntity {
    private String courseId;
    private String lectureId;
    private String name;
    private String roomId;
    private String teacherId;
    private String startTime;
    private String endTime;
    private String day;
    private String category;
    private int Credits;
    private int lateThreshold;

    // Getters and setters
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
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getDay() { return day; }
    public void setDay(String day) {
        try {
            DayOfWeek.valueOf(day.toUpperCase());
            this.day = day.toUpperCase();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid day: " + day);
        }
    }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getCredits() { return Credits; }
    public void setCredits(int credits) { this.Credits = credits; }
    public int getLateThreshold() { return lateThreshold; }
    public void setLateThreshold(int lateThreshold) { this.lateThreshold = lateThreshold; }
}