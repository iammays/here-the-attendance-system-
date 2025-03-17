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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // get all teachers ✅  getAllTeachers()
    // get teacher by id ✅  getTeacherById()
    // get teacher by name ✅ getTeacherByName()
    // get all teachers for a course ✅ getTeachersByCourseId()
    // get teacher by email ✅ getTeacherByEmail()
    // get teacher with courses ✅ getTeacherWithCourses()
    // get all courses for a teacher ✅ getAllCoursesForTeacher()
    // update teacher password by id ✅ updateTeacherPassword()


    @GetMapping
    public List<TeacherEntity> getAllTeachers() {
        return teacherRepository.findAll();
    }

    @GetMapping("/Teacherid/{id}")
    public ResponseEntity<?> getTeacherById(@PathVariable String id) {
        Optional<TeacherEntity> teacher = teacherRepository.findByTeacherId(id);
        return teacher.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<?> getTeacherByName(@PathVariable String name) {
        List<TeacherEntity> teachers = teacherRepository.findByNameContainingIgnoreCase(name);
        if (teachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No teachers found with the given name.");
        }
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/course/{courseId}/all")
    public ResponseEntity<?> getTeachersByCourseId(@PathVariable String courseId) {
        Set<TeacherEntity> teachers = teacherRepository.findByCourseId(courseId);
        if (teachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No teachers found for this course.");
        }
        return ResponseEntity.ok(teachers);
    }

    // @GetMapping("/email/{email}")
    // public ResponseEntity<Map<String, Object>> getTeacherByEmail(@PathVariable String email) {
    //     Optional<TeacherEntity> teacher = teacherRepository.findByEmail(email);
    //     if (teacher.isPresent()) {
    //         return ResponseEntity.ok(Collections.singletonMap("teacher", teacher.get()));
    //     }
    //     return ResponseEntity.status(HttpStatus.NOT_FOUND)
    //     .body(Collections.singletonMap("message", "Teacher not found with this email."));
    // }

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

    @GetMapping("/courses/{teacherId}")
    public ResponseEntity<?> getAllCoursesForTeacher(@PathVariable String teacherId) {
        TeacherEntity teacher = teacherRepository.findByTeacherId(teacherId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));

        List<CourseEntity> courses = courseRepository.findByTeacherId(teacherId);

        System.out.println("Teacher ID: " + teacherId);
        System.out.println("Number of courses found: " + courses.size());

        for (CourseEntity course : courses) {
            System.out.println("Course ID: " + course.getCourseId() + ", Teacher ID: " + course.getTeacherId());
        }
    
        if (courses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No courses found for this teacher.");
        }
    
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<?> getTeacherWithCourses(@PathVariable String id) {
        TeacherEntity teacher = teacherRepository.findByTeacherId(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));

        List<CourseEntity> courses = courseRepository.findByTeacherId(id);

        return ResponseEntity.ok(Map.of("teacher", teacher, "courses", courses));
    }

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



 
@GetMapping("/{teacherId}/schedule")
public ResponseEntity<?> getAllSchedulesForTeacher(@PathVariable String teacherId) {
    try {
        // Fetch all courses taught by this teacher
        List<CourseEntity> courses = courseRepository.findByTeacherId(teacherId);
        
        if (courses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No courses found for this teacher.");
        }

        // Initialize the schedule map
        Map<String, List<CourseEntity>> scheduleMap = new LinkedHashMap<>();
        scheduleMap.put("Monday", new ArrayList<>());
        scheduleMap.put("Tuesday", new ArrayList<>());
        scheduleMap.put("Wednesday", new ArrayList<>());
        scheduleMap.put("Thursday", new ArrayList<>());
        scheduleMap.put("Friday", new ArrayList<>());

        // Group the courses by day of the week
        for (CourseEntity course : courses) {
            String dayOfWeek = course.getDay();
            if (scheduleMap.containsKey(dayOfWeek)) {
                scheduleMap.get(dayOfWeek).add(course);
            }
        }

        // Return the schedule map
        return ResponseEntity.ok(scheduleMap);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching schedule.");
    }
}




@PutMapping("/update-schedules")
public ResponseEntity<?> updateAllTeachersSchedules() {
    try {
        // Fetch all teachers
        List<TeacherEntity> teachers = teacherRepository.findAll();
        if (teachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No teachers found.");
        }

        // Iterate over all teachers and update their schedules
        for (TeacherEntity teacher : teachers) {
            List<CourseEntity> courses = courseRepository.findByTeacherId(teacher.getTeacherId());

            // Initialize the schedule map
            Map<String, List<CourseEntity>> scheduleMap = new LinkedHashMap<>();
            scheduleMap.put("Monday", new ArrayList<>());
            scheduleMap.put("Tuesday", new ArrayList<>());
            scheduleMap.put("Wednesday", new ArrayList<>());
            scheduleMap.put("Thursday", new ArrayList<>());
            scheduleMap.put("Friday", new ArrayList<>());

            // Group courses by day and update the schedule
            for (CourseEntity course : courses) {
                String dayOfWeek = course.getDay();
                if (scheduleMap.containsKey(dayOfWeek)) {
                    scheduleMap.get(dayOfWeek).add(course);
                }
            }

            // Update the teacher's schedule list
            teacher.setSchedulelist(scheduleMap);
            teacherRepository.save(teacher);
        }

        return ResponseEntity.ok("Schedules updated for all teachers.");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating schedules.");
    }
}


}