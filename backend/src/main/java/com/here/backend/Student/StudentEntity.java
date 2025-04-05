// backend/src/main/java/com/here/backend/Student/StudentEntity.java
package com.here.backend.Student;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document(collection = "students")
public class StudentEntity {

    @Id
    private String studentId;               // رقم الطالب
    private String name;                    // اسم الطالب
    private String teacherId;               // معرف المعلم (غير مستخدم حالياً للطلاب، يمكن حذفه إذا لزم)
    private String email;                   // إيميل الطالب
    private String advisor;                 // معرف المستشار
    private List<String> courseId;          // قائمة معرفات الكورسات
    private Map<String, Integer> courseAbsences;        // عدد الغيابات لكل كورس
    private Map<String, String> courseAttendanceStatus; // حالة الحضور لكل كورس
    private Map<String, Boolean> courseWfStatus;        // حالة WF لكل كورس

    // Default Constructor
    public StudentEntity() {
        this.courseId = new ArrayList<>();
        this.courseAbsences = new HashMap<>();
        this.courseAttendanceStatus = new HashMap<>();
        this.courseWfStatus = new HashMap<>();
    }

    // Parameterized Constructor
    public StudentEntity(String studentId, String name, String email, String advisor, List<String> courseId) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.advisor = advisor;
        this.courseId = (courseId != null) ? courseId : new ArrayList<>();
        this.courseAbsences = new HashMap<>();
        this.courseAttendanceStatus = new HashMap<>();
        this.courseWfStatus = new HashMap<>();
        if (courseId != null) {
            for (String course : courseId) {
                this.courseAbsences.put(course, 0);           // تهيئة الغيابات بـ 0
                this.courseAttendanceStatus.put(course, "Absent"); // حالة افتراضية
                this.courseWfStatus.put(course, false);       // WF افتراضي false
            }
        }
    }

    // Getters and Setters
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
        this.courseAttendanceStatus.clear();
        this.courseWfStatus.clear();
        if (courseId != null) {
            for (String course : courseId) {
                this.courseAbsences.put(course, 0);
                this.courseAttendanceStatus.put(course, "Absent");
                this.courseWfStatus.put(course, false);
            }
        }
    }
    public Map<String, Integer> getCourseAbsences() { return courseAbsences; }
    public void setCourseAbsences(Map<String, Integer> courseAbsences) {
        this.courseAbsences = (courseAbsences != null) ? courseAbsences : new HashMap<>();
    }
    public Map<String, String> getCourseAttendanceStatus() { return courseAttendanceStatus; }
    public void setCourseAttendanceStatus(Map<String, String> courseAttendanceStatus) {
        this.courseAttendanceStatus = (courseAttendanceStatus != null) ? courseAttendanceStatus : new HashMap<>();
    }
    public Map<String, Boolean> getCourseWfStatus() { return courseWfStatus; }
    public void setCourseWfStatus(Map<String, Boolean> courseWfStatus) {
        this.courseWfStatus = (courseWfStatus != null) ? courseWfStatus : new HashMap<>();
    }
    public void addCourseAbsence(String courseId) {
        this.courseAbsences.put(courseId, this.courseAbsences.getOrDefault(courseId, 0) + 1);
    }
    public void addCourseWfStatus(String courseId, Boolean wfStatus) {
        this.courseWfStatus.put(courseId, wfStatus);
    }
    public void addCourseAttendanceStatus(String courseId, String attendanceStatus) {
        this.courseAttendanceStatus.put(courseId, attendanceStatus);
    }
    public void removeCourseAbsence(String courseId) { this.courseAbsences.remove(courseId); }
    public void removeCourseWfStatus(String courseId) { this.courseWfStatus.remove(courseId); }
    public void removeCourseAttendanceStatus(String courseId) { this.courseAttendanceStatus.remove(courseId); }
}