package com.here.backend.Student;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "students")
public class StudentEntity {

    @Id
    private String StudentId;
    private String name;
    private String email;
    private String advisor;
    private List<String> CourseId;

    // Constructors
    public StudentEntity() {}

    public StudentEntity(String name, String email, String advisorName, List<String> courseId) {
        this.name = name;
        this.email = email;
        this.advisor = advisorName;
        this.CourseId = courseId;
    }

    public String getStudentId() {
        return StudentId;
    }

    public void setStudentId(String student_id) {
        StudentId = student_id;
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

    public String getAdvisor() {
        return advisor;
    }

    public void setAdvisor(String advisor) {
        this.advisor = advisor;
    }

    public List<String> getCourseId() {
        return CourseId;
    }

    public void setCourseId(List<String> courseid) {
        CourseId = courseid;
    }
}