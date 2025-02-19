package com.here.backend.Student;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "students")
public class StudentEntity {

    @Id
    private String id;

    private String name;

    private String email;

    private String advisorName;

    private List<String> courseIds;

    // Constructors
    public StudentEntity() {}

    public StudentEntity(String name, String email, String advisorName, List<String> courseIds) {
        this.name = name;
        this.email = email;
        this.advisorName = advisorName;
        this.courseIds = courseIds;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAdvisorName() {
        return advisorName;
    }

    public void setAdvisorName(String advisorName) {
        this.advisorName = advisorName;
    }

    public List<String> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<String> courseIds) {
        this.courseIds = courseIds;
    }
}
