package com.here.backend.Teacher;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "teachers")
public class TeacherEntity {
    @Id
    private String teacherId;
    private String name;
    private String email;
    private String password;
    private List<String> courseId;

    // Constructor
    public TeacherEntity() {}

    public TeacherEntity(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public TeacherEntity(String name, String email, String password, List<String> courseIds) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.courseId = courseIds;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherid) {
        this.teacherId = teacherid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getCourseId() {
        return courseId; 
    }

    public void setCourseId(List<String> courseid) {
        this.courseId = courseid;
    }
}