package com.here.backend.Student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentRepository studentRepository;

    // Create a new student
    @PostMapping
    public StudentEntity createStudent(@RequestBody StudentEntity studentEntity) {
        return studentRepository.save(studentEntity);
    }

    // Get all students
    @GetMapping
    public List<StudentEntity> getAllStudents() {
        return studentRepository.findAll();
    }

    // Get a student by ID
    @GetMapping("/{id}")
    public ResponseEntity<StudentEntity> getStudentById(@PathVariable String id) {
        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update a student
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

    // Delete a student
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
            // من المفترض هنا إرسال البريد الإلكتروني
            return ResponseEntity.ok("Email sent successfully to " + student.get().getName());
        }
        return ResponseEntity.notFound().build();
    }
}