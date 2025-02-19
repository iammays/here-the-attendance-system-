package com.here.backend.Course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    // Create a new course
    @PostMapping
    public CourseEntity createCourse(@RequestBody CourseEntity courseEntity) {
        return courseRepository.save(courseEntity);
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

    // Update a course
    @PutMapping("/{id}")
    public ResponseEntity<CourseEntity> updateCourse(@PathVariable String id, @RequestBody CourseEntity courseDetails) {
        return courseRepository.findById(id)
                .map(course -> {
                    course.setCourseName(courseDetails.getCourseName());
                    course.setTeacherId(courseDetails.getTeacherId());
                    course.setRoomIds(courseDetails.getRoomIds());
                    return ResponseEntity.ok(courseRepository.save(course));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a course
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
        return courseRepository.findById(id)
                .map(course -> {
                    courseRepository.delete(course);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
