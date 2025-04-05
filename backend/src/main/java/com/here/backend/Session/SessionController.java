package com.here.backend.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

    @Autowired
    private SessionRepository sessionRepository;

    // Create a new session
    @PostMapping
    public SessionEntity createSession(@RequestBody SessionEntity sessionEntity) {
        return sessionRepository.save(sessionEntity);
    }

    // Get all sessions
    @GetMapping
    public List<SessionEntity> getAllSessions() {
        return sessionRepository.findAll();
    }

    // Get a session by ID
    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionEntity> getSessionById(@PathVariable String sessionId) {
        return sessionRepository.findById(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update a session
    @PutMapping("/{sessionId}")
    public ResponseEntity<SessionEntity> updateSession(@PathVariable String sessionId, @RequestBody SessionEntity sessionDetails) {
        return sessionRepository.findById(sessionId)
                .map(session -> {
                    session.setCourseId(sessionDetails.getCourseId());
                    session.setRoomId(sessionDetails.getRoomId());
                    session.setStartTime(sessionDetails.getStartTime());
                    session.setEndTime(sessionDetails.getEndTime());
                    session.setStatus(sessionDetails.getStatus());
                    return ResponseEntity.ok(sessionRepository.save(session));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a session
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        return sessionRepository.findById(sessionId)
                .map(session -> {
                    sessionRepository.delete(session);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}