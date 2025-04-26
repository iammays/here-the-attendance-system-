// backend/src/main/java/com/here/backend/Attendance/AttendanceRepository.java
package com.here.backend.Attendance;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AttendanceRepository extends MongoRepository<AttendanceEntity, String> {
    AttendanceEntity findByLectureIdAndStudentId(String lectureId, String studentId);
    
    @Query(value = "{'lectureId': ?0}", delete = true)
    void deleteByLectureId(String lectureId);
    List<AttendanceEntity> findByLectureId(String lectureId); // تعليق: جلب كل سجلات الحضور لمحاضرة معينة
    List<AttendanceEntity> findByStudentId(String studentId); // تعليق: جلب كل سجلات الحضور لطالب معين
}