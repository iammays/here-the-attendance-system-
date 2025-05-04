package com.here.backend.Course;

import com.here.backend.Lecture.LectureEntity;
import com.here.backend.Lecture.LectureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    private final CourseRepository courseRepository;
    private final LectureRepository lectureRepository;
    private final RestTemplate restTemplate;

    public CourseController(CourseRepository courseRepository, LectureRepository lectureRepository, RestTemplate restTemplate) {
        this.courseRepository = courseRepository;
        this.lectureRepository = lectureRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public ResponseEntity<List<CourseEntity>> getAllCourses() {
        logger.info("Fetching all courses");
        List<CourseEntity> courses = courseRepository.findAll();
        logger.debug("Retrieved {} courses", courses.size());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable String id) {
        logger.info("Fetching course or lecture with id: {}", id);
        Optional<LectureEntity> lecture = lectureRepository.findByLectureId(id);
        if (lecture.isPresent()) {
            logger.debug("Found lecture with id: {}", id);
            return ResponseEntity.ok(lecture.get());
        }
        Optional<CourseEntity> course = courseRepository.findByCourseIdAndLectureIdIsNull(id);
        if (course.isPresent()) {
            logger.debug("Found course with id: {}", id);
            return ResponseEntity.ok(course.get());
        }
        logger.warn("No course or lecture found with id: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<CourseEntity>> getCoursesByName(@PathVariable String name) {
        logger.info("Fetching courses by name: {}", name);
        List<CourseEntity> courses = courseRepository.findByName(name).stream()
                .filter(c -> c.getLectureId() == null)
                .collect(Collectors.toList());
        if (courses.isEmpty()) {
            logger.warn("No courses found with name: {}", name);
            return ResponseEntity.notFound().build();
        }
        logger.debug("Found {} courses with name: {}", courses.size(), name);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseEntity>> getCoursesByTeacher(@PathVariable String teacherId) {
        logger.info("Fetching courses by teacherId: {}", teacherId);
        List<CourseEntity> courses = courseRepository.findByTeacherId(teacherId).stream()
                .filter(c -> c.getLectureId() == null)
                .collect(Collectors.toList());
        logger.debug("Found {} courses for teacherId: {}", courses.size(), teacherId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}/name")
    public ResponseEntity<String> getCourseNameById(@PathVariable String id) {
        logger.info("Fetching course name by id: {}", id);
        Optional<CourseEntity> course = courseRepository.findByCourseIdAndLectureIdIsNull(id);
        if (course.isPresent()) {
            logger.debug("Found course name: {} for id: {}", course.get().getName(), id);
            return ResponseEntity.ok(course.get().getName());
        }
        logger.warn("No course found with id: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseEntity>> getCourseByCategory(@PathVariable String category) {
        logger.info("Fetching courses by category: {}", category);
        List<CourseEntity> courses = courseRepository.findByCategory(category).stream()
                .filter(c -> c.getLectureId() == null)
                .collect(Collectors.toList());
        logger.debug("Found {} courses in category: {}", courses.size(), category);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/name/{courseName}/category/{category}")
    public ResponseEntity<List<CourseEntity>> getCourseByNameAndCategory(@PathVariable String courseName, @PathVariable String category) {
        logger.info("Fetching courses by name: {} and category: {}", courseName, category);
        List<CourseEntity> courses = courseRepository.findByNameAndCategory(courseName, category).stream()
                .filter(c -> c.getLectureId() == null)
                .collect(Collectors.toList());
        logger.debug("Found {} courses for name: {} and category: {}", courses.size(), courseName, category);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/name/{courseName}/teacher/{teacherId}")
    public ResponseEntity<List<CourseEntity>> getCourseByNameAndTeacher(@PathVariable String courseName, @PathVariable String teacherId) {
        logger.info("Fetching courses by name: {} and teacherId: {}", courseName, teacherId);
        List<CourseEntity> courses = courseRepository.findByNameAndTeacherId(courseName, teacherId).stream()
                .filter(c -> c.getLectureId() == null)
                .collect(Collectors.toList());
        logger.debug("Found {} courses for name: {} and teacherId: {}", courses.size(), courseName, teacherId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/day/{day}")
    public ResponseEntity<List<CourseEntity>> getCourseByDay(@PathVariable String day) {
        logger.info("Fetching courses by day: {}", day);
        List<CourseEntity> courses = courseRepository.findByDay(day).stream()
                .filter(c -> c.getLectureId() == null)
                .collect(Collectors.toList());
        logger.debug("Found {} courses on day: {}", courses.size(), day);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}/time")
    public ResponseEntity<Integer> getCourseTimeById(@PathVariable String id) {
        logger.info("Fetching course duration for id: {}", id);
        Optional<CourseEntity> courseOpt = courseRepository.findByCourseIdAndLectureIdIsNull(id);
        if (!courseOpt.isPresent()) {
            logger.warn("Course not found for id: {}", id);
            return ResponseEntity.notFound().build();
        }
        CourseEntity course = courseOpt.get();
        try {
            if (course.getStartTime() == null || course.getEndTime() == null) {
                logger.error("StartTime or EndTime is null for course: {}", id);
                return ResponseEntity.badRequest().body(0);
            }
            int duration = calculateDuration(course.getStartTime(), course.getEndTime());
            logger.debug("Calculated duration: {} minutes for course: {}", duration, id);
            return ResponseEntity.ok(duration);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid time format for course: {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(0);
        }
    }

    private int calculateDuration(String startTime, String endTime) {
        try {
            LocalTime start = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime end = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
            return (int) java.time.Duration.between(start, end).toMinutes();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format: " + startTime + " or " + endTime, e);
        }
    }

    @PostMapping("/startCamera/{courseId}")
    public ResponseEntity<String> startCamera(
            @PathVariable String courseId,
            @RequestBody Map<String, Object> body) {
        logger.info("Starting camera for courseId: {}", courseId);
        String lectureId = (String) body.get("lecture_id");
        Integer lectureDuration = (Integer) body.get("lecture_duration");
        Integer lateThreshold = (Integer) body.get("late_threshold");
        Integer interval = (Integer) body.get("interval");
        String videoPath = (String) body.get("video_path");

        if (lectureId == null || lectureDuration == null || lateThreshold == null || interval == null || videoPath == null) {
            logger.error("Missing required parameters for starting camera for courseId: {}", courseId);
            return ResponseEntity.badRequest().body("Missing required parameters");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("lecture_id", lectureId);
        params.put("lecture_duration", lectureDuration);
        params.put("late_threshold", lateThreshold);
        params.put("interval", interval);
        params.put("video_path", videoPath);

        try {
            restTemplate.postForObject("http://localhost:5000/start", params, String.class);
            logger.info("Camera started successfully for lecture: {}", lectureId);
            return ResponseEntity.ok("Camera started for lecture " + lectureId);
        } catch (Exception e) {
            logger.error("Failed to start camera for lecture: {}: {}", lectureId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start camera: " + e.getMessage());
        }
    }

    @PutMapping("/{courseId}/lateThreshold")
    public ResponseEntity<String> updateLateThreshold(@PathVariable String courseId, @RequestBody Map<String, Integer> body) {
        logger.info("Updating late threshold for courseId: {}", courseId);
        Integer lateThreshold = body.get("lateThreshold");
        if (lateThreshold == null || lateThreshold < 0) {
            logger.error("Invalid lateThreshold value: {} for courseId: {}", lateThreshold, courseId);
            return ResponseEntity.badRequest().body("lateThreshold is required and must be non-negative");
        }

        Optional<CourseEntity> courseOpt = courseRepository.findByCourseIdAndLectureIdIsNull(courseId);
        if (!courseOpt.isPresent()) {
            logger.warn("Course not found for courseId: {}", courseId);
            return ResponseEntity.badRequest().body("Course not found for courseId: " + courseId);
        }
        CourseEntity course = courseOpt.get();
        course.setLateThreshold(lateThreshold);
        courseRepository.save(course);
        logger.info("Late threshold updated to {} seconds for courseId: {}", lateThreshold, courseId);
        return ResponseEntity.ok("Late threshold updated to " + lateThreshold + " seconds for course " + courseId);
    }

    @GetMapping("/days/{courseName}")
    public ResponseEntity<List<String>> getDaysByCourseName(@PathVariable String courseName) {
        logger.info("Fetching days for courseName: {}", courseName);
        List<CourseEntity> courses = courseRepository.findByName(courseName).stream()
                .filter(c -> c.getLectureId() == null)
                .collect(Collectors.toList());
        if (courses.isEmpty()) {
            logger.warn("No courses found for courseName: {}", courseName);
            return ResponseEntity.notFound().build();
        }
        Set<String> uniqueDays = courses.stream()
                .map(CourseEntity::getDay)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        logger.debug("Found {} unique days for courseName: {}", uniqueDays.size(), courseName);
        return ResponseEntity.ok(new ArrayList<>(uniqueDays));
    }

    @GetMapping("/{courseId}/lectures")
    public ResponseEntity<List<LectureEntity>> getLecturesByCourse(@PathVariable String courseId) {
        logger.info("Fetching all lectures for courseId: {}", courseId);
        List<LectureEntity> lectures = lectureRepository.findAll().stream()
                .filter(l -> l.getCourseId().equals(courseId))
                .collect(Collectors.toList());
        logger.debug("Found {} lectures for courseId: {}", lectures.size(), courseId);
        return ResponseEntity.ok(lectures);
    }

    @GetMapping("/{courseId}/lectures/date/{date}")
    public ResponseEntity<List<LectureEntity>> getLecturesByCourseAndDate(
            @PathVariable String courseId,
            @PathVariable String date) {
        logger.info("Fetching lectures for courseId: {} and date: {}", courseId, date);
        try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            List<LectureEntity> lectures = lectureRepository.findAll().stream()
                    .filter(l -> l.getCourseId().equals(courseId) && l.getLectureId().contains(date))
                    .collect(Collectors.toList());
            logger.debug("Found {} lectures for courseId: {} and date: {}", lectures.size(), courseId, date);
            return ResponseEntity.ok(lectures);
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format: {} for courseId: {}", date, courseId);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/{courseId}/upcoming-classes")
    public ResponseEntity<List<Map<String, Object>>> getUpcomingClasses(@PathVariable String courseId) {
        logger.info("Fetching upcoming classes for courseId: {}", courseId);
        List<LectureEntity> lectures = lectureRepository.findAll().stream()
                .filter(l -> l.getCourseId().equals(courseId))
                .collect(Collectors.toList());

        List<Map<String, Object>> upcomingClasses = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (LectureEntity lecture : lectures) {
            String[] lectureParts = lecture.getLectureId().split("-");
            if (lectureParts.length < 3) {
                logger.warn("Invalid lectureId format: {} for courseId: {}", lecture.getLectureId(), courseId);
                continue;
            }

            String dateStr = lectureParts[1];
            try {
                LocalDate lectureDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                if (lectureDate.isAfter(today) || lectureDate.equals(today)) {
                    Map<String, Object> classInfo = new HashMap<>();
                    classInfo.put("courseName", lecture.getName());
                    classInfo.put("roomId", lecture.getRoomId());
                    classInfo.put("dateTime", lectureDate.atTime(
                            LocalTime.parse(lecture.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"))).toString());
                    upcomingClasses.add(classInfo);
                }
            } catch (DateTimeParseException e) {
                logger.error("Invalid date format in lectureId: {} for courseId: {}", lecture.getLectureId(), courseId);
                continue;
            }
        }

        upcomingClasses.sort(Comparator.comparing(m -> (String) m.get("dateTime")));
        logger.debug("Found {} upcoming classes for courseId: {}", upcomingClasses.size(), courseId);
        return ResponseEntity.ok(upcomingClasses);
    }
}