//backend\src\main\java\com\here\backend\Room\RoomController.java

package com.here.backend.Room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    // Create or update a room with ID and course ID
    @PostMapping
    public ResponseEntity<RoomEntity> createOrUpdateRoom(@RequestBody RoomEntity roomEntity) {
        RoomEntity savedRoom = roomRepository.save(roomEntity);
        return ResponseEntity.ok(savedRoom);
    }

    // Get all rooms
    @GetMapping
    public List<RoomEntity> getAllRooms() {
        return roomRepository.findAll();
    }

    // Get a room by ID
    @GetMapping("/{id}")
    public ResponseEntity<RoomEntity> getRoomById(@PathVariable String id) {
        return roomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
