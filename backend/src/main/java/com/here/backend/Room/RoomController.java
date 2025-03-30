//backend\src\main\java\com\here\backend\Room\RoomController.java

package com.here.backend.Room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.here.backend.Schedual.SchedualEntity;
import com.here.backend.Schedual.SchedualRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

        @Autowired
    private SchedualRepository schedualRepository;


    //there are some errors
    // Create or update a room with ID and course ID
    @PostMapping
    public ResponseEntity<RoomEntity> createOrUpdateRoom(@RequestBody RoomEntity roomEntity) {
        RoomEntity savedRoom = roomRepository.save(roomEntity);
        return ResponseEntity.ok(savedRoom);
    }
    //not tested
    // Get all rooms
    @GetMapping
    public List<RoomEntity> getAllRooms() {
        return roomRepository.findAll();
    }
    // there is some errors
    // Get a room by ID
    @GetMapping("/{id}")
    public ResponseEntity<RoomEntity> getRoomById(@PathVariable String id) {
        return roomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    //not tested
    // Delete a room by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomById(@PathVariable String id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    //not tested
    // Update a room by ID
    @PutMapping("/{id}")
    public ResponseEntity<RoomEntity> updateRoom(@PathVariable String id, @RequestBody RoomEntity roomEntity) {
        if (roomRepository.existsById(id)) {
            roomEntity.setRoom_id(id);  // Ensure the room_id is set correctly for update
            RoomEntity updatedRoom = roomRepository.save(roomEntity);
            return ResponseEntity.ok(updatedRoom);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    
    // Find rooms by schedule ID
    @GetMapping("/schedule/{schedule_id}")
    public List<RoomEntity> getRoomsByScheduleId(@PathVariable String schedule_id) {
        return roomRepository.findByScheduleId(schedule_id);
    }


        @GetMapping("/{roomId}/schedule")
    public ResponseEntity<?> getRoomSchedule(@PathVariable String roomId) {
        // Find schedule for the given room ID
        List<SchedualEntity> schedules = schedualRepository.findByRoomId(roomId);
        
        if (schedules.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(schedules);
    }

@GetMapping("/all-with-sessions")
public ResponseEntity<?> getAllRoomsWithSessions() {
    List<RoomEntity> rooms = roomRepository.findAll();
    List<SchedualEntity> schedules = schedualRepository.findAll();

    // Use a LinkedHashSet to remove duplicate room IDs while maintaining order
    Set<String> uniqueRoomIds = rooms.stream()
            .map(RoomEntity::getRoom_id)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    // Map roomId to its corresponding list of schedules
    Map<String, List<SchedualEntity>> roomScheduleMap = schedules.stream()
            .collect(Collectors.groupingBy(SchedualEntity::getRoomId));

    // Build response
    List<Object> result = uniqueRoomIds.stream().map(roomId -> {
        List<Map<String, Object>> schedulesWithSessions = roomScheduleMap.getOrDefault(roomId, List.of())
                .stream()
                .map(schedule -> Map.of(
                        "schedule_id", schedule.getScheduleId(),
                        "sessionsList", schedule.getListOfSessions() // Assuming getListOfSessions() returns the list of sessions
                ))
                .collect(Collectors.toList());

        return Map.of(
                "room_id", roomId,
                "schedules", schedulesWithSessions
        );
    }).collect(Collectors.toList());

    return ResponseEntity.ok(result);
}

    

}
