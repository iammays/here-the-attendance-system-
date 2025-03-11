//backend\src\main\java\com\here\backend\Attendance\AttendanceRepository.java

package com.here.backend.Attendance;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AttendanceRepository extends MongoRepository<AttendanceEntity, String> {

    AttendanceEntity findByLectureIdAndStudentId(String lectureId, String studentId);
    // You can add custom query methods here if needed
}
