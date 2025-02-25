package com.here.backend.Course;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends MongoRepository<CourseEntity, String> {
    List<CourseEntity> findByTeacherId(String teacherId);
    List<CourseEntity> findByCourseName(String courseName);
    List<CourseEntity> findByCategory(String category);
    List<CourseEntity> findByCourseNameAndCategory(String courseName, String category);
    List<CourseEntity> findByCourseNameAndTeacherId(String courseName, String teacherId);
    List<CourseEntity> findByStudentIds(String studentId);
    List<CourseEntity> findByDepartment(String departmentId);
    List<CourseEntity> findBySemester(String semester);
}
