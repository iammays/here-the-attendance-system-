package com.here.backend.Student;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.*;

@Document(collection = "students")
public class StudentEntity {

    @Id
    private String studentId;
    private String name;
    private String teacherId;
    private String email;
    private String advisor;
    private List<String> courseId;
    private Map<String, Integer> courseAbsences; // Map of course ID to absence count
    private Map<String, Boolean> courseWfStatus; // New: Map of course ID to WF status

    // Default Constructor
    public StudentEntity() {
        this.courseAbsences = new HashMap<>(); // Ensure courseAbsences is always initialized
        this.courseWfStatus = new HashMap<>();
    }

    // Parameterized Constructor
    public StudentEntity(String studentId, String name, String email, String advisorName, List<String> courseId) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.advisor = advisorName;
        this.courseId = courseId;
        this.courseAbsences = new HashMap<>();
        // Initialize the courseAbsences map with course IDs and default absence count of 0
        if (courseId != null) {
            for (String course : courseId) {
                this.courseAbsences.put(course, 0); // Default absence is 0
            }
        }
        this.courseWfStatus = new HashMap<>();
        // Initialize the courseAbsences map with course IDs and default absence count of 0
        if (courseId != null) {
            for (String course : courseId) {
                this.courseWfStatus.put(course, false); // Default absence is 0
            }
        }
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdvisor() {
        return advisor;
    }

    public void setAdvisor(String advisor) {
        this.advisor = advisor;
    }

    public List<String> getCourseId() {
        return courseId;
    }

    public void setCourseId(List<String> courseId) {
        this.courseId = courseId;
        // Re-initialize courseAbsences map with course IDs and default absence count of 0
        this.courseAbsences.clear();
        this.courseWfStatus.clear();

        if (courseId != null) {
            for (String course : courseId) {
                this.courseAbsences.put(course, 0); // Default absence is 0
                this.courseWfStatus.put(course, false); // Default absence is 0
            }
        }
    }

    public Map<String, Integer> getCourseAbsences() {
        return courseAbsences;
    }

    public void setCourseAbsences(Map<String, Integer> courseAbsences) {
        this.courseAbsences = (courseAbsences != null) ? courseAbsences : new HashMap<>();
    }

    public Map<String, Boolean> getCourseWfStatus() {
        return courseWfStatus;
    }

    public void setCourseWfStatus(Map<String, Boolean> courseWfStatus) {
        this.courseWfStatus = (courseWfStatus != null) ? courseWfStatus : new HashMap<>();
    }
}
