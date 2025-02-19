package com.here.backend.Teacher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers") // Base URL for the Teacher API
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepository;

    @GetMapping
    public List<TeacherEntity> getAllTeachers() {
        return teacherRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherEntity> getTeacherById(@PathVariable String id) {
        return teacherRepository.findById(id)
                .map(teacher -> ResponseEntity.ok(teacher))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TeacherEntity createTeacher(@RequestBody TeacherEntity teacher) {
        return teacherRepository.save(teacher);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherEntity> updateTeacher(@PathVariable String id, @RequestBody TeacherEntity teacherDetails) {
        return teacherRepository.findById(id)
                .map(teacher -> {
                    teacher.setName(teacherDetails.getName());
                    teacher.setEmail(teacherDetails.getEmail());
                    return ResponseEntity.ok(teacherRepository.save(teacher));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteTeacher(@PathVariable String id) {
    //     return teacherRepository.findById(id)
    //             .map(teacher -> {
    //                 teacherRepository.delete(teacher);
    //                 return ResponseEntity.noContent().build();
    //             })
    //             .orElse(ResponseEntity.notFound().build());
    // }
}
