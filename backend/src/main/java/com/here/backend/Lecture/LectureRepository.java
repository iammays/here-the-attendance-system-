package com.here.backend.Lecture;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface LectureRepository extends MongoRepository<LectureEntity, String> {
    Optional<LectureEntity> findByLectureId(String lectureId);
}