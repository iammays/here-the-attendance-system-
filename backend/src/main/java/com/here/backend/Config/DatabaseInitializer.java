package com.here.backend.Config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
        // Create collections if they do not exist
        mongoTemplate.createCollection("rooms");
        mongoTemplate.createCollection("teachers");
        mongoTemplate.createCollection("students");
        mongoTemplate.createCollection("attendances");
        mongoTemplate.createCollection("cameras");
        mongoTemplate.createCollection("courses");
        mongoTemplate.createCollection("emails");
        mongoTemplate.createCollection("excellimports");
        mongoTemplate.createCollection("facerecognitions");
        mongoTemplate.createCollection("scheduals");
        mongoTemplate.createCollection("sessions");
        // Add more collections as needed
    }
}
