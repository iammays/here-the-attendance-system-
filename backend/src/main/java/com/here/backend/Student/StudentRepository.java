package com.here.backend.Student;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface StudentRepository extends MongoRepository<StudentEntity, String> {
    Optional<StudentEntity> findByStudentId(String id);

    List<StudentEntity> findByName(String name);

    List<StudentEntity> findByAdvisor(String advisorName);

    List<StudentEntity> findByTeacherId(String teacherId);

    List<StudentEntity> findByCourseId(String courseId);

    List<StudentEntity> findByEmail(String email);


    @Query("{ 'name' : { $regex: ?0, $options: 'i' } }")
    List<StudentEntity> findByNameRegex(String name);

}