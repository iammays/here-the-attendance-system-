package com.here.backend.Attendance;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface AttendanceRepository extends MongoRepository<AttendanceEntity, String> {
    AttendanceEntity findByLectureIdAndStudentId(String lectureId, String studentId);
    
    @Query(value = "{'lectureId': ?0}", delete = true)
    void deleteByLectureId(String lectureId);
}