package com.here.backend.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final MongoTemplate mongoTemplate;

    @Autowired
    public DatabaseInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // List of all collections you want to create
        String[] collections = {
            "rooms", "teachers", "students", "attendances", "cameras",
            "courses", "emails", "excellimports", "facerecognitions", "scheduals", "sessions", "lectures"
        };

        // Create each collection ONLY if it does not exist
        for (String collectionName : collections) {
            if (!mongoTemplate.collectionExists(collectionName)) {
                mongoTemplate.createCollection(collectionName);
                logger.info("Created collection: {}", collectionName);
            } else {
                logger.info("Collection already exists: {}", collectionName);
            }
        }
    }
}