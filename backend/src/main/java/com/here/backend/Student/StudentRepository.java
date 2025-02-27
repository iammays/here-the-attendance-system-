package com.here.backend.Student;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudentRepository extends MongoRepository<StudentEntity, String> {
    
    List<StudentEntity> findByName(String name);
    List<StudentEntity> findByAdvisorName(String advisorName);
    // List<StudentEntity> findByMajor(String major);
    // List<StudentEntity> findByGpa(double gpa);
    List<StudentEntity> findByCourseIds(String courseId);
    List<StudentEntity> findByemail(String email);
    Optional<StudentEntity> findById(String id);
}
