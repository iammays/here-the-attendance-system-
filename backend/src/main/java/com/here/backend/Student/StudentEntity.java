package com.here.backend.Student;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "students")
public class StudentEntity {

    @Id
    private String studentId;
    private String name;
    private String teacherId;
    private String email;
    private String advisor;
    private List<String> courseId;

    public StudentEntity() {}

    public StudentEntity(String studentId, String name, String email, String advisorName, List<String> courseId) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.advisor = advisorName;
        this.courseId = courseId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String student_id) {
        studentId = student_id;
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

    public void setTeacherId(String teacher_id) {
        teacherId = teacher_id;
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

    public void setCourseId(List<String> courseid) {
        courseId = courseid;
    }
}