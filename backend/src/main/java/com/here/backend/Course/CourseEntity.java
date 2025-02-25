package com.here.backend.Course;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "courses")
public class CourseEntity {
    @Id
    private String courseId;
    private String courseName;
    private String category;  // التصنيف (مثل "Humanity")
    private String semester; 
    private String department;  
    private String teacherId;
    private List<String> roomIds;
    private List<String> studentIds;// coursese in students

    
    public CourseEntity() {}

    public CourseEntity(String name, String category, String teacherId) {
        this.courseName = name;
        this.category = category;
        this.teacherId = teacherId;
    }


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
    
    public String getCategory() { 
        return category; 
        
    }
    
    public void setCategory(String category) { 
        this.category = category; 
    }
    
    public List<String> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(List<String> roomIds) {
        this.roomIds = roomIds;
    }

    public List<String> getStudentIds() {
        return studentIds;
    }
    
    public void setStudentIds(List<String> studentIds) {
        this.studentIds = studentIds;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
