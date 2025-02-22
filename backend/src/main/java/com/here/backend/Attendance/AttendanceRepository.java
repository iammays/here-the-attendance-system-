package com.here.backend.Attendance;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AttendanceRepository extends MongoRepository<AttendanceEntity, String> {
    // You can add custom query methods here if needed
}
