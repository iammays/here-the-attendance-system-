package com.here.backend.Student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/students")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CourseRepository courseRepository;
    
    public StudentController(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository; 
    }

        // get student by id ✅ getStudentById()  //done
        // get student by name ✅ getStudentByName() //done
        // get student by email ✅ getStudentByEmail() //done
        // get all students ✅ getAllStudents() //done
        // get all students in a specific course ✅ getStudentsByCourse() //done
        // get all courses for a student ✅ getAllCoursesForStudent() //done
        // get all students with advisorID ✅ getStudentsByAdvisor()    //done
        // search on student name ✅ searchStudentsByName() //done
        // add student in a course ✅ addStudentInCourse() //done


        //------------------------------------------------------------
        // get absences for a specific student ✅ getStudentAbsences()  //done
        // post absences for a specific student ✅ postStudentAbsences()  //done
        // get all student with them statuse in a specific course ✅  getStudentsByAttendanceStatus() //done
        // post attendance for a specific student ✅ postStudentAttendance() //done
        // get wf status for a specific student in a specific course ✅ getWfStatusForCourse() //done



    @GetMapping("/id/{id}")
    public ResponseEntity<StudentEntity> getStudentById(@PathVariable String id) {
        return studentRepository.findByStudentId(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<StudentEntity>> getStudentByName(@PathVariable String name) {
        List<StudentEntity> students = studentRepository.findByName(name);
        return students.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(students);
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudentEntity>> searchStudentsByName(@RequestParam String name) {
        List<StudentEntity> students = studentRepository.findByNameRegex(name);
        return students.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(students);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<StudentEntity> getStudentByEmail(@PathVariable String email) {
        Optional<StudentEntity> student = studentRepository.findByEmail(email).stream().findFirst();
        return student.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<StudentEntity> getAllStudents() {
        return studentRepository.findAll();
    }

    @GetMapping("/course/{courseId}")
    public List<StudentEntity> getStudentsByCourse(@PathVariable String  courseId) {
        return studentRepository.findByCourseId(courseId);
    }

    @GetMapping("/{id}/courses") 
    public List<CourseEntity> getAllCoursesForStudent(@PathVariable String id) {
        Optional<StudentEntity> student = studentRepository.findByStudentId(id);

        System.out.println("Received Student ID: " + id);
    
        if (student.isPresent()) {
            List<String> courseIds = student.get().getCourseId();
    
            // System.out.println("Course IDs: " + courseIds); 
    
            if (courseIds == null || courseIds.isEmpty()) {
                return Collections.emptyList();
            }
    
            List<CourseEntity> courses = courseRepository.findByCourseIdIn(courseIds); 
            // System.out.println("Courses found: " + courses); 

            for (CourseEntity course : courses) {
                course.setStudentId(id);  // تعيين studentId يدويًا
            }
    
            return courses;
        }
    
        return Collections.emptyList();
    }

    @GetMapping("/advisor/{advisorName}")
    public List<StudentEntity> getStudentsByAdvisor(@PathVariable String advisorName) {
        return studentRepository.findByAdvisor(advisorName);
    }

    // @PostMapping
    // public StudentEntity createStudent(@RequestBody StudentEntity studentEntity) {
    //     return studentRepository.save(studentEntity);
    // }

    @PostMapping("/course/{courseId}/students/{studentId}")
    public ResponseEntity<?> addStudentInCourse(@PathVariable String courseId, @PathVariable String studentId) {
        Optional<StudentEntity> student = studentRepository.findByStudentId(studentId);
        if (student.isPresent()) {
            StudentEntity updatedStudent = student.get();
            List<String> courses = updatedStudent.getCourseId();
            if (!courses.contains(courseId)) {
                courses.add(courseId);
                updatedStudent.setCourseId(courses);
                studentRepository.save(updatedStudent);
                return ResponseEntity.ok("Student enrolled in course successfully.");
            }
            return ResponseEntity.badRequest().body("Student is already enrolled in this course.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found.");
    }

    //-----------------------------------------------------------
    @GetMapping("/{studentId}/absences")
    public ResponseEntity<Map<String, Integer>> getStudentAbsences(@PathVariable String studentId) {
        Optional<StudentEntity> student = studentRepository.findByStudentId(studentId);
            return student.map(s -> ResponseEntity.ok(s.getCourseAbsences()))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{studentId}/absences/{courseId}")
    public ResponseEntity<?> postStudentAbsences(
    @PathVariable String studentId,
    @PathVariable String courseId,
    @RequestBody Map<String, Integer> requestBody) {

        Optional<StudentEntity> student = studentRepository.findByStudentId(studentId);
        if (student.isPresent()) {
            StudentEntity updatedStudent = student.get();
            Map<String, Integer> absencesMap = updatedStudent.getCourseAbsences();

            if (requestBody.containsKey("absences")) {
                absencesMap.put(courseId, requestBody.get("absences"));
                updatedStudent.setCourseAbsences(absencesMap);
                studentRepository.save(updatedStudent);
                return ResponseEntity.ok("Absence record updated successfully.");
            }
            return ResponseEntity.badRequest().body("Missing 'absences' field in request body.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found.");
    }

    // @GetMapping("/course/{courseId}/attendance")
    // public ResponseEntity<List<StudentEntity>> getStudentsByAttendanceStatus(
    // @PathVariable String courseId,
    // @RequestParam(name = "status") String status) {

    //     List<StudentEntity> students = studentRepository.findAll();
    //     List<StudentEntity> filteredStudents = new ArrayList<>();

    //     for (StudentEntity student : students) {
    //         Map<String, String> attendance = student.getCourseAttendanceStatus();
    //         if (attendance.containsKey(courseId) && attendance.get(courseId).equalsIgnoreCase(status)) {
    //             filteredStudents.add(student);
    //         }
    //     }

    //     return filteredStudents.isEmpty() 
    //     ? ResponseEntity.notFound().build() 
    //     : ResponseEntity.ok(filteredStudents);
    // }

    // @PostMapping("/{studentId}/attendance/{courseId}")
    // public ResponseEntity<?> postStudentAttendance(
    // @PathVariable String studentId,
    // @PathVariable String courseId,
    // @RequestBody Map<String, String> requestBody) {

    //     Optional<StudentEntity> student = studentRepository.findByStudentId(studentId);
    //     if (student.isPresent()) {
    //         StudentEntity updatedStudent = student.get();
    //         Map<String, String> attendanceMap = updatedStudent.getCourseAttendanceStatus();

    //         if (requestBody.containsKey("status")) {
    //             String status = requestBody.get("status");
    //             List<String> validStatuses = Arrays.asList("Present", "Absent", "Excused", "Late");

    //             if (!validStatuses.contains(status)) {
    //                 return ResponseEntity.badRequest().body("Invalid status. Use Present, Absent, Excused, or Late.");
    //             }

    //             attendanceMap.put(courseId, status);
    //             updatedStudent.setCourseAttendanceStatus(attendanceMap);
    //             studentRepository.save(updatedStudent);
    //             return ResponseEntity.ok("Attendance updated successfully.");
    //         }
    //         return ResponseEntity.badRequest().body("Missing 'status' field in request body.");
    //     }
    //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found.");
    // }

    @GetMapping("/{studentId}/courses/{courseId}/wf-status")
    public ResponseEntity<String> getWfStatusForCourse(
        @PathVariable String studentId,
        @PathVariable String courseId) {
    
        return studentRepository.findById(studentId)
            .map(student -> ResponseEntity.ok(student.getCourseWfStatus().getOrDefault(courseId, "Pending")))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
}