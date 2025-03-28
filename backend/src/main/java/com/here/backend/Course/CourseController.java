package com.here.backend.Course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import org.springframework.http.HttpStatus;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    @Autowired
    private CourseRepository courseRepository;

    // جلب كل المقررات
    // get all courses ✅ getAllCourses()
    @GetMapping
    public List<CourseEntity> getAllCourses() {
        return courseRepository.findAll();
    }

    // جلب مقرر باستخدام المعرف
    // get course by id ✅ getCourseById()
    @GetMapping("/{id}")
    public ResponseEntity<CourseEntity> getCourseById(@PathVariable String id) {
        return courseRepository.findByCourseId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // جلب مقررات باستخدام الاسم
    // get course by name ✅ getCourseByName()
    @GetMapping("/name/{courseName}")
    public ResponseEntity<List<CourseEntity>> getCourseByName(@PathVariable String courseName) {
        List<CourseEntity> courses = courseRepository.findByName(courseName);
        return ResponseEntity.ok(courses);
    }

    // جلب مقررات باستخدام معرف المعلم
    // get courses by teacherId ✅ getCoursesByTeacher()
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseEntity>> getCoursesByTeacher(@PathVariable String teacherId) {
        List<CourseEntity> courses = courseRepository.findByTeacherId(teacherId);
        return ResponseEntity.ok(courses);
    }

    // جلب اسم المقرر باستخدام المعرف
    // get course name by id ✅ getCourseNameById()
    @GetMapping("/{id}/name")
    public ResponseEntity<String> getCourseNameById(@PathVariable String id) {
        return courseRepository.findByCourseId(id)
            .map(course -> ResponseEntity.ok(course.getName()))
            .orElse(ResponseEntity.notFound().build());
    }

    // جلب مقررات باستخدام الفئة
    // get course by category ✅ getCourseByCategory()
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseEntity>> getCourseByCategory(@PathVariable String category) {
        List<CourseEntity> courses = courseRepository.findByCategory(category);
        return ResponseEntity.ok(courses);
    }

    // جلب مقررات باستخدام الاسم والفئة
    // get course by name and category ✅ getCourseByNameAndCategory()
    @GetMapping("/name/{courseName}/category/{category}")
    public ResponseEntity<List<CourseEntity>> getCourseByNameAndCategory(@PathVariable String courseName, @PathVariable String category) {
        List<CourseEntity> courses = courseRepository.findByNameAndCategory(courseName, category);
        return ResponseEntity.ok(courses);
    }

    // جلب مقررات باستخدام الاسم ومعرف المعلم
    // get course by name and teacher ✅ getCourseByNameAndTeacher()
    @GetMapping("/name/{courseName}/teacher/{teacherId}")
    public ResponseEntity<List<CourseEntity>> getCourseByNameAndTeacher(@PathVariable String courseName, @PathVariable String teacherId) {
        List<CourseEntity> courses = courseRepository.findByNameAndTeacherId(courseName, teacherId);
        return ResponseEntity.ok(courses);
    }

    // جلب مقررات باستخدام اليوم
    // get courses by day ✅ getCourseByDay()
    @GetMapping("/day/{day}")
    public ResponseEntity<List<CourseEntity>> getCourseByDay(@PathVariable String day) {
        List<CourseEntity> courses = courseRepository.findByDay(day);
        return ResponseEntity.ok(courses);
    }

    // جلب مدة المقرر بالدقائق باستخدام المعرف
    // get time of course by id in MINUTES ✅ getCourseTimeById()
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

    // تحويل الوقت إلى دقائق من صيغة HH:mm
    private int convertTimeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return (hours * 60) + minutes;
    }

    //for mays dont delete this
    @Autowired
    private CourseService courseService;
    @Autowired
    private RestTemplate restTemplate;

    // إنشاء مقرر جديد
    @PostMapping
    public ResponseEntity<CourseEntity> createCourse(@RequestBody CourseEntity course) {
        CourseEntity savedCourse = courseRepository.save(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse);
    }

    // تشغيل الكاميرا لمحاضرة معينة وإرسال البيانات لـ Flask
    @PostMapping("/startCamera/{courseId}")
    public ResponseEntity<String> startCamera(@PathVariable String courseId, @RequestParam int lateThreshold, @RequestBody Map<String, Object> body) {
        String lectureId = (String) body.get("lecture_id");
        int lectureDuration = (int) body.get("lecture_duration");
        int lateThresholdSeconds = (int) body.get("late_threshold");
        int interval = (int) body.get("interval");
        String videoPath = (String) body.get("video_path");

        Map<String, Object> params = new HashMap<>();
        params.put("lecture_id", lectureId);
        params.put("lecture_duration", lectureDuration);
        params.put("late_threshold", lateThresholdSeconds);
        params.put("interval", interval);
        params.put("video_path", videoPath);

        restTemplate.postForObject("http://localhost:5000/start", params, String.class);
        return ResponseEntity.ok("Camera started for lecture " + lectureId);
    }

    // حساب مدة المحاضرة بالدقائق من وقت البداية والنهاية
    private int calculateDuration(String startTime, String endTime) {
        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");
        int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
        int endMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);
        return endMinutes - startMinutes;
    }

    // لما تشبكي الداتابيس: جلب البيانات من CourseEntity
    /*
    CourseEntity course = courseRepository.findByCourseId(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
    int lectureDuration = calculateDuration(course.getStartTime(), course.getEndTime());
    int lateThresholdSeconds = lateThreshold * 60;
    CameraSchedule schedule = courseService.calculateCameraSchedule(courseId, lateThreshold);
    int interval = schedule.getInterval();
    String videoPath = course.getVideoPath(); // لو أضفتِ حقل videoPath في CourseEntity
    */
}