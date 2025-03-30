
// package com.here.backend.Schedual;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Controller;

// import com.here.backend.Course.CourseEntity;
// import com.here.backend.Course.CourseRepository;
// import com.here.backend.Room.RoomRepository;

// import io.netty.handler.codec.http.HttpResponseStatus;

// import org.springframework.http.HttpStatus;
// import org.springframework.web.bind.annotation.*;




// @RestController
// @RequestMapping("/api/scheduals")
// public class SchedualController {
    
//   @Autowired
//     private SchedualRepository SchedualRepository;

//     @Autowired
//     private CourseRepository CourseRepository;



//     // create schedule row
//     @PostMapping
//     public ResponseEntity<SchedualEntity> createSchedule(@RequestBody SchedualEntity schedualEntity) {
//         SchedualEntity savedEntity = SchedualRepository.save(schedualEntity);
//         return ResponseEntity.status(HttpStatus.CREATED).body(savedEntity);
//     }

// // gets all the schedules
// @GetMapping
// public List<SchedualEntity> getschedules() {
//     return SchedualRepository.findAll();
// }

// // 
// @GetMapping("/{id}")
// public ResponseEntity<SchedualEntity> getschedulebyid(@PathVariable String id) {

//     return SchedualRepository.findById(id)
//     .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
// }



// @GetMapping("/sessions/{roomid}")
// public List<CourseEntity> getCoursesByRoomId(@PathVariable String roomid) {
//     System.out.println("Received roomId: " + roomid);
//     List<CourseEntity> coursesInSession = CourseRepository.findByRoomId(roomid);
//     return coursesInSession;
// }



// }




//backend\src\main\java\com\here\backend\Schedual\SchedualController.java

package com.here.backend.Schedual;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Room.RoomEntity;
import com.here.backend.Room.RoomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedules")
public class SchedualController {

    @Autowired
    private SchedualRepository schedualRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CourseRepository courseRepository;
    @PostMapping("/populate")
    public List<SchedualEntity> populateSchedules() {
        List<RoomEntity> rooms = roomRepository.findAll();
        
        // Create a list to hold the schedules without redundant roomIds
        List<SchedualEntity> schedules = new ArrayList<>();
        
        // For each room, find all courses that belong to this room
        rooms.forEach(room -> {
            // Find courses that match the roomId (streaming inside the courseRepository)
            List<CourseEntity> matchedCourses = courseRepository.findByRoomId(room.getRoom_id());
            
            // Check if this roomId already exists in the schedules list
            boolean roomExists = schedules.stream().anyMatch(schedule -> schedule.getRoomId().equals(room.getRoom_id()));
            
            if (!roomExists) {
                // If not, create a new schedule and add it to the list
                SchedualEntity newSchedule = new SchedualEntity(room.getScheduleId(), room.getRoom_id(), matchedCourses);
                schedules.add(newSchedule);
            }
        });
        
        // Save the list of schedules with unique roomIds
        return schedualRepository.saveAll(schedules);
    }
    
    // @PostMapping("/populate")
    // public List<SchedualEntity> populateSchedules() {
    //     List<RoomEntity> rooms = roomRepository.findAll();
    //     List<CourseEntity> courses = courseRepository.findAll();
        
    //     List<SchedualEntity> schedules = rooms.stream().map(room -> {
    //         List<CourseEntity> matchedCourses = courses.stream()
    //                 .filter(course -> course.getRoomId().equals(room.getRoom_id()))
    //                 .collect(Collectors.toList());
    //         return new SchedualEntity(room.getScheduleId(), room.getRoom_id(), matchedCourses);
    //     }).collect(Collectors.toList());
        
        
    //     return schedualRepository.saveAll(schedules);
    // }
@GetMapping("/preview")
public List<SchedualEntity> previewSchedules() {
    List<RoomEntity> rooms = roomRepository.findAll();
    
    // Create a list to hold the schedules without redundant roomIds
    List<SchedualEntity> schedules = new ArrayList<>();
    
    // For each room, find all courses that belong to this room
    rooms.forEach(room -> {
        // Find courses that match the roomId (streaming inside the courseRepository)
        List<CourseEntity> matchedCourses = courseRepository.findByRoomId(room.getRoom_id());
        
        // Check if this roomId already exists in the schedules list
        boolean roomExists = schedules.stream().anyMatch(schedule -> schedule.getRoomId().equals(room.getRoom_id()));
        
        if (!roomExists) {
            // If not, create a new schedule and add it to the list
            SchedualEntity newSchedule = new SchedualEntity(room.getScheduleId(), room.getRoom_id(), matchedCourses);
            schedules.add(newSchedule);
        }
    });
    
    // Return the final list of schedules without duplicates
    return schedules;
}

}
