//backend\src\main\java\com\here\backend\Attendance\AttendanceRepository.java

package com.here.backend.Attendance;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface AttendanceRepository extends MongoRepository<AttendanceEntity, String> {
    // البحث عن سجل حضور باستخدام معرف المحاضرة ومعرف الطالب
    AttendanceEntity findByLectureIdAndStudentId(String lectureId, String studentId);
    
    // حذف كل سجلات الحضور لمحاضرة معينة
    @Query(value = "{'lectureId': ?0}", delete = true)
    void deleteByLectureId(String lectureId);
}