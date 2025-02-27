package com.here.backend.Teacher;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TeacherRepository extends MongoRepository<TeacherEntity, String> {
    Optional<TeacherEntity> findByUsername(String username);
    Optional<TeacherEntity> findById(String id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<TeacherEntity> findByEmail(String email);
    Optional<TeacherEntity> findByEmailAndPassword(String email, String password);
    
}