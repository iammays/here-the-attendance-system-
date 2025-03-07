package com.here.backend.Schedual;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Room.RoomRepository;

import io.netty.handler.codec.http.HttpResponseStatus;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/scheduals")
public class SchedualController {
    
  @Autowired
    private SchedualRepository SchedualRepository;

    @Autowired
    private CourseRepository CourseRepository;

    @PostMapping
    public ResponseEntity<SchedualEntity> createSchedule(@RequestBody SchedualEntity schedualEntity) {
        SchedualEntity savedEntity = SchedualRepository.save(schedualEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntity);
    }


@GetMapping
public List<SchedualEntity> getMethodName() {
    return SchedualRepository.findAll();
}

@GetMapping("/{id}")
public ResponseEntity<SchedualEntity> putMethodName(@PathVariable String id, @RequestBody SchedualEntity schedualEntity) {

    return SchedualRepository.findById(id)
    .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
}



@GetMapping("/sessions/{roomid}")
public List<CourseEntity> getCoursesByRoomId(@PathVariable String roomid) {
    System.out.println("Received roomId: " + roomid);
    List<CourseEntity> coursesInSession = CourseRepository.findByRoomId(roomid);
    return coursesInSession;
}







}
