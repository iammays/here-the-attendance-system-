package com.here.backend.Course;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends MongoRepository<CourseEntity, String> {
    // Additional query methods can be defined here if needed
}
