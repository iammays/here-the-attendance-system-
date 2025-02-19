package com.here.backend.Session;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionRepository extends MongoRepository<SessionEntity, String> {
    // You can add custom query methods here if needed
}
