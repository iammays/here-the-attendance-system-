package com.here.backend.Teacher;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TeacherRepository extends MongoRepository<TeacherEntity, String> {

    Optional<TeacherEntity> findByName(String username);
    Optional<TeacherEntity> findByTeacherId(String id);
    boolean existsByName(String username);
    boolean existsByEmail(String email);
    Optional<TeacherEntity> findByEmail(String email);
    Optional<TeacherEntity> findByEmailAndPassword(String email, String password);
    List<TeacherEntity> findByNameContainingIgnoreCase(String name);
    Set<TeacherEntity> findByCourseId(String courseId);


default boolean validatePassword(String name, String rawPassword) {
    Optional<TeacherEntity> teacher = findByName(name);
    return teacher.isPresent() && 
           org.springframework.security.crypto.bcrypt.BCrypt.checkpw(rawPassword, teacher.get().getPassword());
}

}