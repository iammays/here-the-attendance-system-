package com.here.backend.Student;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudentRepository extends MongoRepository<StudentEntity, String> {
    // You can add custom query methods here if needed
}
