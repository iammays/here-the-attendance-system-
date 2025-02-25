package com.here.backend.Teacher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Security.payload.response.MessageResponse;
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
    private final CourseRepository courseRepository;
    @Autowired
    private PasswordEncoder encoder;

    public TeacherController(TeacherRepository teacherRepository, CourseRepository courseRepository) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
    }

    // get all teachers ✅
    // get all teacher for a course
    // get teacher by id //401
    // get teacher by name //401
    // get teacher by email //---
    // get teacher by course
    // get teacher with courses ✅
    // get teacher courses
    // create teacher ✅
    // create course for teacher by id ✅
    // update teacher by id
    // update teacher password by id
    // delete teacher by id
    // get username and pass for test

    @GetMapping //✅
    public List<TeacherEntity> getAllTeachers() {
        return teacherRepository.findAll();
    }

    @GetMapping("/course/{courseId}/all")
    public ResponseEntity<?> getAllTeachersForCourse(@PathVariable String courseId) {
        List<TeacherEntity> teachers = teacherRepository.findByCourseIds(courseId);
        if (teachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No teachers found for this course.");
        }
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeacherById(@PathVariable String id) {
        Optional<TeacherEntity> teacher = teacherRepository.findById(id);
        return teacher.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getTeacherByName(@PathVariable String name) {
        List<TeacherEntity> teachers = teacherRepository.findByNameContainingIgnoreCase(name);
        if (teachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No teachers found with the given name.");
        }
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/{email}")
    public ResponseEntity<Map<String, Object>> getTeacherByEmail(@RequestParam String email) {
        Optional<TeacherEntity> teacher = teacherRepository.findByEmail(email);
        if (teacher.isPresent()) {
            return ResponseEntity.ok(Collections.singletonMap("teacher", teacher.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Collections.singletonMap("message", "Teacher not found with this email."));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getTeachersByCourse(@PathVariable String courseId) {
        List<TeacherEntity> teachers = teacherRepository.findByCourseIds(courseId);
        if (teachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No teachers found for this course.");
        }
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/{id}/courses/all")
    public ResponseEntity<?> getTeacherWithCourses(@PathVariable String id) {
        Optional<TeacherEntity> teacher = teacherRepository.findById(id);
        if (teacher.isPresent()) {
            List<CourseEntity> courses = courseRepository.findByTeacherId(id);
            Map<String, Object> response = new HashMap<>();
            response.put("teacher", teacher.get());
            response.put("courses", courses);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<?> getTeacherCourses(@PathVariable String id) {
        List<CourseEntity> courses = courseRepository.findByTeacherId(id);
        if (courses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No courses found for this teacher.");
        }
        return ResponseEntity.ok(courses);
    }

    // @PostMapping
    // public TeacherEntity createTeacher(@RequestBody TeacherEntity teacher) {
    //     return teacherRepository.save(teacher);
    // }

    @PostMapping("/{id}/courses")
    public ResponseEntity<?> createCourseForTeacher(@PathVariable String id, 
    @RequestBody CourseEntity course, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: You are not allowed to create a course for this teacher.");
        }

        Optional<TeacherEntity> teacher = teacherRepository.findById(id);
        if (teacher.isPresent()) {
            course.setTeacherId(id);
            CourseEntity savedCourse = courseRepository.save(course);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Teacher not found.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable String id, @RequestBody TeacherEntity updatedTeacher,
    @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: You are not allowed to update this profile.");
        }

        Optional<TeacherEntity> existingTeacher = teacherRepository.findById(id);
        if (existingTeacher.isPresent()) {
            TeacherEntity teacher = existingTeacher.get();
            teacher.setName(updatedTeacher.getName());
            teacher.setEmail(updatedTeacher.getEmail());
            teacher.setPassword(encoder.encode(updatedTeacher.getPassword()));
            teacherRepository.save(teacher);
            return ResponseEntity.ok(teacher);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> updateTeacherPassword(@PathVariable String id, @RequestBody String newPassword, 
    @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: You are not allowed to update this password.");
        }

        Optional<TeacherEntity> teacher = teacherRepository.findById(id);
        if (teacher.isPresent()) {
            TeacherEntity updatedTeacher = teacher.get();
            updatedTeacher.setPassword(encoder.encode(newPassword));
            teacherRepository.save(updatedTeacher);
            return ResponseEntity.ok("Password updated successfully.");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeacher(@PathVariable String id, 
    @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: You are not allowed to delete this account.");
        }

        if (teacherRepository.existsById(id)) {
            teacherRepository.deleteById(id);
            return ResponseEntity.ok(new MessageResponse("Teacher deleted successfully!"));
        }
        return ResponseEntity.notFound().build();
    }
}