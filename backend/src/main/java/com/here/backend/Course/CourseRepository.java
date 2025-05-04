package com.here.backend.Course;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<CourseEntity, String> {
    List<CourseEntity> findByTeacherId(String teacherId);

    List<CourseEntity> findByStudentId(String studentId);

    Optional<CourseEntity> findByCourseId(String courseId);

    Optional<CourseEntity> findByLectureId(String lectureId);

    List<CourseEntity> findByCourseIdIn(List<String> courseIds);

    List<CourseEntity> findByName(String courseName);

    List<CourseEntity> findByCategory(String category);

    List<CourseEntity> findByNameAndCategory(String courseName, String category);

    List<CourseEntity> findByNameAndTeacherId(String courseName, String teacherId);

    List<CourseEntity> findByDay(String day);

    List<CourseEntity> findByRoomId(String roomId);

    String findNameByCourseId(String courseId);

    @Query("{ 'courseId': ?0, 'lectureId': { $exists: false } }")
Optional<CourseEntity> findByCourseIdAndLectureIdDoesNotExist(String courseId);


    Optional<CourseEntity> findByCourseIdAndLectureIdIsNull(String courseId);

    List<String> findDistinctDaysByName(String courseName);
}