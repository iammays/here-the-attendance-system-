package com.here.backend.Attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    // Create a new attendance record
    @PostMapping
    public AttendanceEntity createAttendance(@RequestBody AttendanceEntity attendanceEntity) {
        return attendanceRepository.save(attendanceEntity);
    }

    // Get all attendance records
    @GetMapping
    public List<AttendanceEntity> getAllAttendances() {
        return attendanceRepository.findAll();
    }

    // Get an attendance record by ID
    @GetMapping("/{attendanceId}")
    public ResponseEntity<AttendanceEntity> getAttendanceById(@PathVariable String attendanceId) {
        return attendanceRepository.findById(attendanceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update an attendance record
    @PutMapping("/{attendanceId}")
    public ResponseEntity<AttendanceEntity> updateAttendance(@PathVariable String attendanceId, @RequestBody AttendanceEntity attendanceDetails) {
        return attendanceRepository.findById(attendanceId)
                .map(attendance -> {
                    attendance.setStudentId(attendanceDetails.getStudentId());
                    attendance.setSessionId(attendanceDetails.getSessionId());
                    attendance.setStatus(attendanceDetails.getStatus());
                    attendance.setDetectedTime(attendanceDetails.getDetectedTime());
                    return ResponseEntity.ok(attendanceRepository.save(attendance));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete an attendance record
    @DeleteMapping("/{attendanceId}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable String attendanceId) {
        return attendanceRepository.findById(attendanceId)
                .map(attendance -> {
                    attendanceRepository.delete(attendance);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
