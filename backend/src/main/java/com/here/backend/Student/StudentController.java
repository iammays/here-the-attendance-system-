package com.here.backend.Student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/students")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CourseRepository courseRepository;

    // جلب طالب باستخدام المعرف
    // get student by id ✅ getStudentById()
    @GetMapping("/id/{id}")
    public ResponseEntity<StudentEntity> getStudentById(@PathVariable String id) {
        return studentRepository.findByStudentId(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
    }

    // جلب طلاب باستخدام الاسم
    // get student by name ✅ getStudentByName()
    @GetMapping("/name/{name}")
    public ResponseEntity<List<StudentEntity>> getStudentByName(@PathVariable String name) {
        List<StudentEntity> students = studentRepository.findByName(name);
        return students.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(students);
    }

    // جلب طالب باستخدام البريد الإلكتروني
    // get student by email ✅ getStudentByEmail()
    @GetMapping("/email/{email}")
    public ResponseEntity<StudentEntity> getStudentByEmail(@PathVariable String email) {
        Optional<StudentEntity> student = studentRepository.findByEmail(email).stream().findFirst();
        return student.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // جلب كل الطلاب
    // get all students ✅ getAllStudents()
    @GetMapping
    public List<StudentEntity> getAllStudents() {
        return studentRepository.findAll();
    }

    // جلب طلاب مسجلين في مقرر معين
    // get all students in a specific course ✅ getStudentsByCourse()
    @GetMapping("/course/{courseId}")
    public List<StudentEntity> getStudentsByCourse(@PathVariable String courseId) {
        return studentRepository.findByCourseId(courseId);
    }

    // جلب كل المقررات للطالب
    // get all courses for a student ✅ getAllCoursesForStudent()
    @GetMapping("/{id}/courses")
    public List<CourseEntity> getAllCoursesForStudent(@PathVariable String id) {
        Optional<StudentEntity> student = studentRepository.findByStudentId(id);
    
        if (student.isPresent()) {
            List<String> courseIds = student.get().getCourseId();
    
            if (courseIds == null || courseIds.isEmpty()) {
                return Collections.emptyList();
            }
    
            List<CourseEntity> courses = courseRepository.findByCourseIdIn(courseIds);
            return courses;
        }
    
        return Collections.emptyList();
    }

    // جلب طلاب باستخدام اسم المستشار
    // get all students with advisorID ✅ getStudentsByAdvisor()
    @GetMapping("/advisor/{advisorName}")
    public List<StudentEntity> getStudentsByAdvisor(@PathVariable String advisorName) {
        return studentRepository.findByAdvisor(advisorName);
    }

    // إنشاء طالب جديد
    // Create a new student ✅ createStudent()
    @PostMapping
    public StudentEntity createStudent(@RequestBody StudentEntity studentEntity) {
        return studentRepository.save(studentEntity);
    }

    // إضافة طالب لمقرر
    // Add a student to a course addStudentInCourse()
    @PostMapping("/course/{courseId}/students/{studentId}")
    public ResponseEntity<?> addStudentInCourse(@PathVariable String courseId, @PathVariable String studentId) {
        Optional<StudentEntity> student = studentRepository.findByStudentId(studentId);
        if (student.isPresent()) {
            StudentEntity updatedStudent = student.get();
            List<String> courses = updatedStudent.getCourseId();
            if (!courses.contains(courseId)) {
                courses.add(courseId);
                updatedStudent.setCourseId(courses);
                studentRepository.save(updatedStudent);
                return ResponseEntity.ok("Student enrolled in course successfully.");
            }
            return ResponseEntity.badRequest().body("Student is already enrolled in this course.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found.");
    }
}