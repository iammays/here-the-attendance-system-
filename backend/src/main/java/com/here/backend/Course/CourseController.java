package com.here.backend.Course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private RestTemplate restTemplate;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public List<CourseEntity> getAllCourses() {
        return courseRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseEntity> getCourseById(@PathVariable String id) {
        // البحث أولاً باستخدام lectureId
        Optional<CourseEntity> courseByLectureId = courseRepository.findByLectureId(id);
        if (courseByLectureId.isPresent()) {
            return ResponseEntity.ok(courseByLectureId.get());
        }
        // إذا لم يتم العثور على lectureId، البحث باستخدام courseId
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
    public ResponseEntity<?> getCourseTimeById(@PathVariable String id) {
        return courseRepository.findByCourseId(id)
                .map(course -> {
                    try {
                        if (course.getStartTime() == null || course.getEndTime() == null) {
                            return ResponseEntity.badRequest().build();
                        }
    
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

    @PostMapping("/manual")
    public ResponseEntity<CourseEntity> createManualLecture(@RequestBody CourseEntity course) {
        String date = LocalDate.now().toString();
        String startTime = course.getStartTime().replace(":", "");
        String lectureId = course.getCourseId() + "-" + date + "-" + startTime;
        course.setLectureId(lectureId);
        CourseEntity savedCourse = courseRepository.save(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse);
    }

    @PostMapping("/startCamera/{courseId}")
    public ResponseEntity<String> startCamera(
            @PathVariable String courseId,
            @RequestBody Map<String, Object> body) {
        String lectureId = (String) body.get("lecture_id");
        int lectureDuration = (int) body.get("lecture_duration");
        int lateThreshold = (int) body.get("late_threshold");
        int interval = (int) body.get("interval");
        String videoPath = (String) body.get("video_path");

        Map<String, Object> params = new HashMap<>();
        params.put("lecture_id", lectureId);
        params.put("lecture_duration", lectureDuration);
        params.put("late_threshold", lateThreshold);
        params.put("interval", interval);
        params.put("video_path", videoPath);

        restTemplate.postForObject("http://localhost:5000/start", params, String.class);
        return ResponseEntity.ok("Camera started for lecture " + lectureId);
    }

    @PutMapping("/{courseId}/lateThreshold")
    public ResponseEntity<String> updateLateThreshold(@PathVariable String courseId, @RequestBody Map<String, Integer> body) {
        Integer lateThreshold = body.get("lateThreshold");
        if (lateThreshold == null || lateThreshold < 0) {
            return ResponseEntity.badRequest().body("lateThreshold is required and must be non-negative");
        }

        return courseRepository.findByCourseId(courseId)
                .map(course -> {
                    course.setLateThreshold(lateThreshold);
                    courseRepository.save(course);
                    return ResponseEntity.ok("Late threshold updated to " + lateThreshold + " seconds for course " + courseId);
                })
                .orElse(ResponseEntity.badRequest().body("Course not found for courseId: " + courseId));
    }

    @GetMapping("/{courseName}/days")
    public ResponseEntity<List<String>> getCourseDaysByName(@PathVariable String courseName) {
        try {
            List<String> days = courseRepository.findDistinctDaysByName(courseName);
            return ResponseEntity.ok(days);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/days/{courseName}")
    public ResponseEntity<List<String>> getDaysByCourseName(@PathVariable String courseName) {
        List<CourseEntity> courses = courseRepository.findByName(courseName);

        if (courses.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Set<String> uniqueDays = new HashSet<>();
        for (CourseEntity course : courses) {
            uniqueDays.add(course.getDay());
        }

        return ResponseEntity.ok(new ArrayList<>(uniqueDays));
    }
}