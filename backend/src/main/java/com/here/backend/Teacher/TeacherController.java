package com.here.backend.Teacher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Security.payload.response.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/teachers") // Base URL for the Teacher API
public class TeacherController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherController.class);
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private final CourseRepository courseRepository;

    public TeacherController(TeacherRepository teacherRepository, CourseRepository courseRepository) {
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public List<TeacherEntity> getAllTeachers() {
        return teacherRepository.findAll();
    }

    @GetMapping("/{id}/name")
    public ResponseEntity<?> getTeacherName(@PathVariable String id) {
        Optional<TeacherEntity> teacher = teacherRepository.findById(id);
        return teacher.map(value -> ResponseEntity.ok(value.getName()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> updateTeacherPassword(@PathVariable String id, @RequestBody String newPassword) {
        Optional<TeacherEntity> teacher = teacherRepository.findById(id);
        if (teacher.isPresent()) {
            TeacherEntity updatedTeacher = teacher.get();
            updatedTeacher.setPassword(newPassword);
            teacherRepository.save(updatedTeacher);
            return ResponseEntity.ok("Password updated successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeacherById(@PathVariable String id) {
        Optional<TeacherEntity> teacher = teacherRepository.findById(id);
        return teacher.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public TeacherEntity createTeacher(@RequestBody TeacherEntity teacher) {
        return teacherRepository.save(teacher);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable String id, @RequestBody TeacherEntity updatedTeacher) {
        Optional<TeacherEntity> existingTeacher = teacherRepository.findById(id);
        if (existingTeacher.isPresent()) {
            TeacherEntity teacher = existingTeacher.get();
            teacher.setName(updatedTeacher.getName());
            teacher.setEmail(updatedTeacher.getEmail());
            teacher.setPassword(updatedTeacher.getPassword());
            teacherRepository.save(teacher);
            return ResponseEntity.ok(teacher);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeacher(@PathVariable String id) {
        if (teacherRepository.existsById(id)) {
            teacherRepository.deleteById(id);
            return ResponseEntity.ok(new MessageResponse("Teacher deleted successfully!"));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/courses")
    public List<CourseEntity> getTeacherCourses(@PathVariable String id) {
        return courseRepository.findByTeacherId(id);
    }

    @PostMapping("/{id}/courses")
    public CourseEntity createCourse(@PathVariable String id, @RequestBody CourseEntity course) {
        course.setTeacherId(id);
        return courseRepository.save(course);
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
}
