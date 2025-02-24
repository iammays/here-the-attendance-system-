package com.here.backend.Teacher;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "teachers") // Specify the MongoDB collection name
public class TeacherEntity {
    @Id
    private String id;         // Change to String to work with MongoDB
    private String username;
    private String email;
    private String password;;
    private List<String> courseIds;

    // Constructor
    public TeacherEntity() {}

    public TeacherEntity(String username, String email,String password ) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    public TeacherEntity(String username, String email,String password , List<String> courseIds) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.courseIds = courseIds;

    }
    // public TeacherEntity(String id, String username, String email) {
    //     this.id = id;
    //     this.username = username;
    //     this.email = email;
    // }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return username;
    }

    public void setName(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<String> courseIds) {
        this.courseIds = courseIds;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
