//backend\src\main\java\com\here\backend\Teacher\TeacherController.java

package com.here.backend.Teacher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Security.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/teachers")
public class TeacherController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherController.class);
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private  CourseRepository courseRepository;
    @Autowired
    private PasswordEncoder encoder;

    // جلب كل المعلمين
    // get all teachers ✅ getAllTeachers()
    @GetMapping
    public List<TeacherEntity> getAllTeachers() {
        return teacherRepository.findAll();
    }

    // جلب معلم باستخدام المعرف
    // get teacher by id ✅ getTeacherById()
    @GetMapping("/Teacherid/{id}")
    public ResponseEntity<?> getTeacherById(@PathVariable String id) {
        Optional<TeacherEntity> teacher = teacherRepository.findByTeacherId(id);
        return teacher.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // جلب معلم باستخدام الاسم
    // get teacher by name ✅ getTeacherByName()
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getTeacherByName(@PathVariable String name) {
        List<TeacherEntity> teachers = teacherRepository.findByNameContainingIgnoreCase(name);
        if (teachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No teachers found with the given name.");
        }
        return ResponseEntity.ok(teachers);
    }

    // جلب معلمين لمقرر معين
    // get all teachers for a course ✅ getTeachersByCourseId()
    @GetMapping("/course/{courseId}/all")
    public ResponseEntity<?> getTeachersByCourseId(@PathVariable String courseId) {
        Set<TeacherEntity> teachers = teacherRepository.findByCourseId(courseId);
        if (teachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No teachers found for this course.");
        }
        return ResponseEntity.ok(teachers);
    }

    // جلب معلم باستخدام البريد الإلكتروني مع المقررات
    // get teacher by email ✅ getTeacherByEmail()
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getTeacherByEmail(@PathVariable String email) {
        TeacherEntity teacher = teacherRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));

        List<CourseEntity> courses = courseRepository.findByTeacherId(teacher.getTeacherId());

        Map<String, Object> response = new HashMap<>();
        response.put("teacher", teacher);
        response.put("courses", courses);

        return ResponseEntity.ok(response);
    }

    // جلب كل المقررات لمعلم معين
    // get all courses for a teacher ✅ getAllCoursesForTeacher()
    @GetMapping("/courses/{teacherId}")
    public ResponseEntity<?> getAllCoursesForTeacher(@PathVariable String teacherId) {
        TeacherEntity teacher = teacherRepository.findByTeacherId(teacherId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));

        List<CourseEntity> courses = courseRepository.findByTeacherId(teacherId);

        if (courses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No courses found for this teacher.");
        }
    
        return ResponseEntity.ok(courses);
    }

    // جلب معلم مع المقررات الخاصة به
    // get teacher with courses ✅ getTeacherWithCourses()
    @GetMapping("/{id}/courses")
    public ResponseEntity<?> getTeacherWithCourses(@PathVariable String id) {
        TeacherEntity teacher = teacherRepository.findByTeacherId(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));

        List<CourseEntity> courses = courseRepository.findByTeacherId(id);

        return ResponseEntity.ok(Map.of("teacher", teacher, "courses", courses));
    }

    // تغيير كلمة مرور المعلم
    // update teacher password by id ✅ updateTeacherPassword()
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updateTeacherPassword(@PathVariable String id, 
    @RequestBody Map<String, String> passwords, 
    @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: You are not allowed to update this password.");
        }

        Optional<TeacherEntity> teacher = teacherRepository.findByTeacherId(id);
        if (teacher.isPresent()) {
            TeacherEntity updatedTeacher = teacher.get();

            String oldPassword = passwords.get("oldPassword");
            String newPassword = passwords.get("newPassword");

            if (!encoder.matches(oldPassword, updatedTeacher.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Incorrect old password.");
            }

            updatedTeacher.setPassword(encoder.encode(newPassword));
            teacherRepository.save(updatedTeacher);
            return ResponseEntity.ok("Password updated successfully.");
        }
        return ResponseEntity.notFound().build();
    }
}