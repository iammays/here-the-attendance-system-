//backend\src\main\java\com\here\backend\Student\StudentRepository.java

package com.here.backend.Student;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudentRepository extends MongoRepository<StudentEntity, String> {

    List<StudentEntity> findByName(String name);

    List<StudentEntity> findByAdvisor(String advisorName);

    List<StudentEntity> findByTeacherId(String teacherId);

    List<StudentEntity> findByCourseId(String courseId);

    List<StudentEntity> findByEmail(String email);

    Optional<StudentEntity> findByStudentId(String id);
}