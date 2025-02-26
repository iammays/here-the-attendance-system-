package com.here.backend.Student;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudentRepository extends MongoRepository<StudentEntity, String> {
    
    List<StudentEntity> findByName(String name);
    List<StudentEntity> findByAdvisor(String advisorName);
    // List<StudentEntity> findByMajor(String major);
    // List<StudentEntity> findByGpa(double gpa);
    List<StudentEntity> findByCourseId(String courseId);
    List<StudentEntity> findByemail(String email);
    Optional<StudentEntity> findByStudentId(String id);
    List<StudentEntity> findByAdvisorAndCourseId(String advisorName, String courseId);
    //List<StudentEntity> findByCourseIdAndAdvisorName(String courseId, String advisorName);
    List<StudentEntity> findByCourseIdAndAdvisor(String courseId, String advisorName);
}
