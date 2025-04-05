package com.here.backend.Teacher;

import java.util.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.here.backend.Course.CourseEntity;

@Document(collection = "teachers")
public class TeacherEntity {
    
    @Id
    private String teacherId;
    private String name;
    @Indexed(unique = true)
    private String email;
    @JsonIgnore
    private String password;
    private List<String> courseId;
    private Map<String, List<CourseEntity>> schedulelist; // Changed to hold days and courses

    public TeacherEntity() {}

    public TeacherEntity(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public TeacherEntity(String name, String email, String password, List<String> courseId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.courseId = courseId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
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

    public void setCourseId(List<String> courseId) {
        this.courseId = courseId;
    }

    public Map<String, List<CourseEntity>> getSchedulelist() {
        return schedulelist;
    }

    public void setSchedulelist(Map<String, List<CourseEntity>> schedulelist) {
        this.schedulelist = schedulelist;
    }

    @Override
    public String toString() {
        return "TeacherEntity{" +
                "teacherId='" + teacherId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", courseId=" + courseId +
                ", schedulelist=" + schedulelist +
                '}';
    }
}