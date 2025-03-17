package com.here.backend.Attendance;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AttendanceRepository extends MongoRepository<AttendanceEntity, String> {

    long countByStudentIdAndSessionIdAndStatus(String studentId, String courseId, String string);
    // You can add custom query methods here if needed
}
