package com.here.backend.Teacher;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends MongoRepository<TeacherEntity, String> {
    // You can define custom query methods here if needed
}
