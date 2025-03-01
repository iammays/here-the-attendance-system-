package com.here.backend.Course;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<CourseEntity, String> {

    List<CourseEntity> findByTeacherId(String teacherId);
    Optional<CourseEntity> findByCourseId(String courseId);
    List<CourseEntity> findByName(String courseName);
    List<CourseEntity> findByCategory(String category);
    List<CourseEntity> findByNameAndCategory(String courseName, String category);
    List<CourseEntity> findByNameAndTeacherId(String courseName, String teacherId);
    List<CourseEntity> findByCourseId(List<String> courseId);
    List<CourseEntity> findByDay(String day);
}