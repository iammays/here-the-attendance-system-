package com.here.backend.Student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.here.backend.Emails.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/students")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private EmailService emailService;

    // get student by id ✅
    // get all students ✅
    // get all students in a specific course ✅
    // get all courses for a student ✅
    // get all students with advisors ✅
    // get all students with advisors and courses ✅
    // Create a new student ✅
    // add new course for student by id //401
    // add new advisor for student by id --- update advisor name
    // add new student in session
    // add new student in course
    // update student by id
    // update student photo by id
    // delete student by id
    // send email to student by id
    // send email to all students
    // get the number of abcense

    @GetMapping("/{id}")
    public ResponseEntity<StudentEntity> getStudentById(@PathVariable String id) {
        return studentRepository.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<StudentEntity> getAllStudents() {
        return studentRepository.findAll();
    }

    @GetMapping("/course/{courseId}")
    public List<StudentEntity> getStudentsByCourse(@PathVariable String courseId) {
        return studentRepository.findByCourseIds(courseId);
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<?> getAllCoursesForStudent(@PathVariable String id) {
        Optional<StudentEntity> student = studentRepository.findById(id);

        if (student.isPresent()) {
            List<String> courseIds = student.get().getCourseIds();
            return ResponseEntity.ok(courseIds);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body("Student not found with ID: " + id);
    }

    @GetMapping("/advisor/{advisorName}")
    public List<StudentEntity> getStudentsByAdvisor(@PathVariable String advisorName) {
        return studentRepository.findByAdvisorName(advisorName);
    }

    @GetMapping("/advisor/{advisorName}/course/{courseId}")
    public List<StudentEntity> getStudentsByAdvisorAndCourse(@PathVariable String advisorName, @PathVariable String courseId) {
        return studentRepository.findByAdvisorNameAndCourseIds(advisorName, courseId);
    }

    @PostMapping
    public StudentEntity createStudent(@RequestBody StudentEntity studentEntity) {
        return studentRepository.save(studentEntity);
    }

    @PostMapping("/{id}/courses")
    public ResponseEntity<?> addCourseToStudent(@PathVariable String id, @RequestBody String courseId) {
        Optional<StudentEntity> student = studentRepository.findById(id);
        if (student.isPresent()) {
            StudentEntity updatedStudent = student.get();
            List<String> courses = updatedStudent.getCourseIds();
            if (!courses.contains(courseId)) {
                courses.add(courseId);
                updatedStudent.setCourseIds(courses);
                studentRepository.save(updatedStudent);
                return ResponseEntity.ok("Course added successfully to student.");
            }
            return ResponseEntity.badRequest().body("Student is already enrolled in this course.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found.");
    }

    @PutMapping("/{id}/advisor")
    public ResponseEntity<?> updateStudentAdvisor(@PathVariable String id, @RequestBody String advisorName) {
        Optional<StudentEntity> student = studentRepository.findById(id);
        if (student.isPresent()) {
            StudentEntity updatedStudent = student.get();
            updatedStudent.setAdvisorName(advisorName);
            studentRepository.save(updatedStudent);
            return ResponseEntity.ok("Advisor updated successfully.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found.");
    }

    // @PostMapping("/{id}/session")
    // public ResponseEntity<?> addStudentToSession(@PathVariable String id, @RequestBody String sessionId) {
    //     Optional<StudentEntity> student = studentRepository.findById(id);
    //     if (student.isPresent()) {
    //         StudentEntity updatedStudent = student.get();
    //         List<String> sessions = updatedStudent.getSessionIds();
    //         if (!sessions.contains(sessionId)) {
    //             sessions.add(sessionId);
    //             updatedStudent.setSessionIds(sessions);
    //             studentRepository.save(updatedStudent);
    //             return ResponseEntity.ok("Student added to session successfully.");
    //         }
    //         return ResponseEntity.badRequest().body("Student is already in this session.");
    //     }
    //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found.");
    // }

    @PostMapping("/course/{courseId}/students/{studentId}")
    public ResponseEntity<?> enrollStudentInCourse(@PathVariable String courseId, @PathVariable String studentId) {
        Optional<StudentEntity> student = studentRepository.findById(studentId);
        if (student.isPresent()) {
            StudentEntity updatedStudent = student.get();
            List<String> courses = updatedStudent.getCourseIds();
            if (!courses.contains(courseId)) {
                courses.add(courseId);
                updatedStudent.setCourseIds(courses);
                studentRepository.save(updatedStudent);
                return ResponseEntity.ok("Student enrolled in course successfully.");
            }
            return ResponseEntity.badRequest().body("Student is already enrolled in this course.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentEntity> updateStudent(@PathVariable String id, @RequestBody StudentEntity studentDetails) {
        return studentRepository.findById(id)
        .map(student -> {
            student.setName(studentDetails.getName());
            student.setEmail(studentDetails.getEmail());
            student.setAdvisorName(studentDetails.getAdvisorName());
            student.setCourseIds(studentDetails.getCourseIds());
            return ResponseEntity.ok(studentRepository.save(student));
        })
        .orElse(ResponseEntity.notFound().build());
    }

    // @PutMapping("/{id}/photo")
    // public ResponseEntity<?> updateStudentPhoto(@PathVariable String id, @RequestBody String photoUrl) {
    //     Optional<StudentEntity> student = studentRepository.findById(id);
    //     if (student.isPresent()) {
    //         StudentEntity updatedStudent = student.get();
    //         updatedStudent.setPhoto(photoUrl);
    //         studentRepository.save(updatedStudent);
    //         return ResponseEntity.ok("Student photo updated successfully");
    //     }
    //     return ResponseEntity.notFound().build();
    // }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String id) {
        return studentRepository.findById(id)
        .map(student -> {
            studentRepository.delete(student);
            return ResponseEntity.ok().<Void>build();
        })
        .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/email")
    public ResponseEntity<?> sendEmailToStudent(@PathVariable String id, @RequestBody String emailContent) {
        Optional<StudentEntity> student = studentRepository.findById(id);
        if (student.isPresent()) {
            String subject = "Important Update"; // Customize the subject as needed
            emailService.sendEmail(student.get().getEmail(), subject, emailContent);
            return ResponseEntity.ok("Email sent successfully to " + student.get().getName());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/email")
    public ResponseEntity<?> sendEmailToAllStudents(@RequestBody String emailContent) {
        List<StudentEntity> students = studentRepository.findAll();
        for (StudentEntity student : students) {
            String subject = "Important Update"; // Customize the subject as needed
            emailService.sendEmail(student.getEmail(), subject, emailContent);
        }
        return ResponseEntity.ok("Email sent successfully to all students");
    }
}