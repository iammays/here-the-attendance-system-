package com.here.backend.Course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    @Autowired
    private CourseRepository courseRepository;
    
        public CourseController(CourseRepository courseRepository) {
            this.courseRepository = courseRepository;
        }

        // get all courses ✅ getAllCourses()
        // get course by id ✅ getCourseById()
        // get course by name ✅ getCourseByName()
        // get courses by teacherId ✅ getCoursesByTeacher() 
        // get course name by id ✅ getCourseNameById()
        // get course by category ✅ getCourseByCategory()
        // get course by name and category ✅  getCourseByNameAndCategory()
        // get course by name and teacher ✅ getCourseByNameAndTeacher() 
        // get courses by day ✅ getCourseByDay()
        // get time of course by id in MINUTES ✅ getCourseTimeById()

    @GetMapping
    public List<CourseEntity> getAllCourses() {
        return courseRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseEntity> getCourseById(@PathVariable String id) {
        return courseRepository.findByCourseId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{courseName}")
    public ResponseEntity<List<CourseEntity>> getCourseByName(@PathVariable String courseName) {
        List<CourseEntity> courses = courseRepository.findByName(courseName);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseEntity>> getCoursesByTeacher(@PathVariable String teacherId) {
        List<CourseEntity> courses = courseRepository.findByTeacherId(teacherId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}/name")
    public ResponseEntity<String> getCourseNameById(@PathVariable String id) {
        return courseRepository.findByCourseId(id)
            .map(course -> ResponseEntity.ok(course.getName()))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseEntity>> getCourseByCategory(@PathVariable String category) {
        List<CourseEntity> courses = courseRepository.findByCategory(category);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/name/{courseName}/category/{category}")
    public ResponseEntity<List<CourseEntity>> getCourseByNameAndCategory(@PathVariable String courseName, @PathVariable String category) {
        List<CourseEntity> courses = courseRepository.findByNameAndCategory(courseName, category);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/name/{courseName}/teacher/{teacherId}")
    public ResponseEntity<List<CourseEntity>> getCourseByNameAndTeacher(@PathVariable String courseName, @PathVariable String teacherId) {
        List<CourseEntity> courses = courseRepository.findByNameAndTeacherId(courseName, teacherId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/day/{day}")
    public ResponseEntity<List<CourseEntity>> getCourseByDay(@PathVariable String day) {
        List<CourseEntity> courses = courseRepository.findByDay(day);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}/time")
    public ResponseEntity<Integer> getCourseTimeById(@PathVariable String id) {
        return (ResponseEntity<Integer>) courseRepository.findByCourseId(id)
            .map(course -> {
                try {
                    int startMinutes = convertTimeToMinutes(course.getStartTime());
                    int endMinutes = convertTimeToMinutes(course.getEndTime());
                    int duration = endMinutes - startMinutes;
                    return ResponseEntity.ok(duration);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().build();
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }

    private int convertTimeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return (hours * 60) + minutes;
    }
}