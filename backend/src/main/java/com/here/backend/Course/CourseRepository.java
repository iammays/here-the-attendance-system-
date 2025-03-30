//backend\src\main\java\com\here\backend\Course\CourseRepository.java

package com.here.backend.Course;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<CourseEntity, String> {
    // جلب مقررات باستخدام معرف المعلم
    List<CourseEntity> findByTeacherId(String teacherId);
    // جلب مقررات باستخدام معرف الطالب
    List<CourseEntity> findByStudentId(String studentId);
    // جلب مقرر باستخدام المعرف (اختياري)
    Optional<CourseEntity> findByCourseId(String courseId);
    // جلب عدة مقررات باستخدام قائمة معرفات
    List<CourseEntity> findByCourseIdIn(List<String> courseIds);
    // جلب مقررات باستخدام الاسم
    List<CourseEntity> findByName(String courseName);
    // جلب مقررات باستخدام الفئة
    List<CourseEntity> findByCategory(String category);
    // جلب مقررات باستخدام الاسم والفئة
    List<CourseEntity> findByNameAndCategory(String courseName, String category);
    // جلب مقررات باستخدام الاسم ومعرف المعلم
    List<CourseEntity> findByNameAndTeacherId(String courseName, String teacherId);
    // جلب مقررات باستخدام اليوم
    List<CourseEntity> findByDay(String day);
    // جلب مقررات باستخدام معرف الغرفة
    List<CourseEntity> findByRoomId(String roomId);
    String findNameByCourseId(String courseId);
    // int countByCourseId(String courseId);

    int countByCourseName(String courseName);

    // جلب بيانات المقرر باستخدام المعرف
    default List<CourseEntity> getCourseTimeById(String courseId) {
        return findByCourseId(courseId).map(List::of).orElse(List.of());
    }
}