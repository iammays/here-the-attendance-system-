package com.here.backend.Course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseEntity>> getCoursesByTeacher(@PathVariable String teacherId) {
        List<CourseEntity> courses = courseRepository.findByTeacherId(teacherId);
        return ResponseEntity.ok(courses);
    }

    // Create a new course
    // @PostMapping
    // public CourseEntity createCourse(@RequestBody CourseEntity courseEntity) {
    //     return courseRepository.save(courseEntity);
    // }

    @PostMapping
    public ResponseEntity<CourseEntity> addCourse(@RequestBody CourseEntity course) {
        CourseEntity savedCourse = courseRepository.save(course);
        return ResponseEntity.ok(savedCourse);
    }

    // Update a course
    @PutMapping("/{id}")
    public ResponseEntity<CourseEntity> updateCourse(@PathVariable String id, @RequestBody CourseEntity updatedCourse) {
        Optional<CourseEntity> courseData = courseRepository.findById(id);
        if (courseData.isPresent()) {
            CourseEntity course = courseData.get();
            course.setCourseName(updatedCourse.getCourseName());
            course.setCategory(updatedCourse.getCategory());
            course.setTeacherId(updatedCourse.getTeacherId());
            courseRepository.save(course);
            return ResponseEntity.ok(course);
        }
        return ResponseEntity.notFound().build();
    }

    // Get all courses
    @GetMapping
    public List<CourseEntity> getAllCourses() {
        return courseRepository.findAll();
    }

    // Get a course by ID
    @GetMapping("/{id}")
    public ResponseEntity<CourseEntity> getCourseById(@PathVariable String id) {
        return courseRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // // Delete a course
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
    //     return courseRepository.findById(id)
    //             .map(course -> {
    //                 courseRepository.delete(course);
    //                 return ResponseEntity.ok().<Void>build();
    //             })
    //             .orElse(ResponseEntity.notFound().build());
    // }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable String id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return ResponseEntity.ok("Course deleted successfully.");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/category")
    public ResponseEntity<?> getCourseCategory(@PathVariable String id) {
        return courseRepository.findById(id)
            .map(course -> ResponseEntity.ok(course.getCategory()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/name")
    public ResponseEntity<?> getCourseName(@PathVariable String id) {
        return courseRepository.findById(id)
            .map(course -> ResponseEntity.ok(course.getCourseName()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}