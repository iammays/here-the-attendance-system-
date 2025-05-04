// Updated StudentEntity.java
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
    private Map<String, Integer> courseAbsences;
    // private Map<String, String> courseAttendanceStatus;
    private Map<String, String> courseWfStatus; // CHANGED from Boolean to String

    public StudentEntity() {
        this.courseId = new ArrayList<>();
        this.courseAbsences = new HashMap<>();
        // this.courseAttendanceStatus = new HashMap<>();
        this.courseWfStatus = new HashMap<>();
    }

    public StudentEntity(String studentId, String name, String email, String advisor, List<String> courseId) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.advisor = advisor;
        this.courseId = (courseId != null) ? courseId : new ArrayList<>();
        this.courseAbsences = new HashMap<>();
        // this.courseAttendanceStatus = new HashMap<>();
        this.courseWfStatus = new HashMap<>();
        if (courseId != null) {
            for (String course : courseId) {
                this.courseAbsences.put(course, 0);
                // this.courseAttendanceStatus.put(course, "Absent");
                this.courseWfStatus.put(course, "Pending"); 
            }
        }
    }

    // All Getters and Setters...
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAdvisor() { return advisor; }
    public void setAdvisor(String advisor) { this.advisor = advisor; }
    public List<String> getCourseId() { return courseId; }
    public void setCourseId(List<String> courseId) {
        this.courseId = (courseId != null) ? courseId : new ArrayList<>();
        this.courseAbsences.clear();
        // this.courseAttendanceStatus.clear();
        this.courseWfStatus.clear();
        if (courseId != null) {
            for (String course : courseId) {
                this.courseAbsences.put(course, 0);
                // this.courseAttendanceStatus.put(course, "Absent");
                this.courseWfStatus.put(course, "Pending");
            }
        }
    }
    public Map<String, Integer> getCourseAbsences() { return courseAbsences; }
    public void setCourseAbsences(Map<String, Integer> courseAbsences) {
        this.courseAbsences = (courseAbsences != null) ? courseAbsences : new HashMap<>();
    }
    // public Map<String, String> getCourseAttendanceStatus() { return courseAttendanceStatus; }
    // public void setCourseAttendanceStatus(Map<String, String> courseAttendanceStatus) {
    //     this.courseAttendanceStatus = (courseAttendanceStatus != null) ? courseAttendanceStatus : new HashMap<>();
    // }
    public Map<String, String> getCourseWfStatus() { return courseWfStatus; } // CHANGED
    public void setCourseWfStatus(Map<String, String> courseWfStatus) { // CHANGED
        this.courseWfStatus = (courseWfStatus != null) ? courseWfStatus : new HashMap<>();
    }
    public void addCourseAbsence(String courseId) {
        this.courseAbsences.put(courseId, this.courseAbsences.getOrDefault(courseId, 0) + 1);
    }
    public void updateCourseWfStatus(String courseId, String wfStatus) {
        this.courseWfStatus.put(courseId, wfStatus);
    }
    // public void addCourseAttendanceStatus(String courseId, String attendanceStatus) {
    //     this.courseAttendanceStatus.put(courseId, attendanceStatus);
    // }
    public void removeCourseAbsence(String courseId) { this.courseAbsences.remove(courseId); }
    public void removeCourseWfStatus(String courseId) { this.courseWfStatus.remove(courseId); }
    // public void removeCourseAttendanceStatus(String courseId) { this.courseAttendanceStatus.remove(courseId); }
}
