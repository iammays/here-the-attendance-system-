package com.here.backend.Camera;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraRepository extends MongoRepository<CameraEntity, String> {
    // Additional query methods can be defined here if needed
}
