package com.here.backend.Attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private AttendanceService attendanceService;

    // تسجيل حضور طالب جديد
    @PostMapping
    public ResponseEntity<String> saveAttendance(@RequestBody AttendanceRecord record) {
        AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(record.getLectureId(), record.getStudentId());
        if (attendance == null) {
            attendance = new AttendanceEntity();
            attendance.setLectureId(record.getLectureId());
            attendance.setStudentId(record.getStudentId());
            attendance.setSessions(new java.util.ArrayList<>());
        }
        attendance.getSessions().add(new AttendanceEntity.SessionAttendance(record.getSessionId(), record.getDetectionTime()));
        attendance.setStatus(record.getStatus());
        attendanceRepository.save(attendance);
        return ResponseEntity.ok("Attendance saved");
    }

    // جلب تقرير حضور طالب لمحاضرة معينة
    @GetMapping("/{lectureId}/{studentId}")
    public ResponseEntity<?> getAttendanceReport(@PathVariable String lectureId, @PathVariable String studentId, @RequestParam int lateThreshold) {
        String status = attendanceService.determineStatus(lectureId, studentId, lateThreshold);
        int detectionCount = attendanceService.countDetections(lectureId, studentId);
        AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, studentId);
        return ResponseEntity.ok(new AttendanceReport(status, detectionCount, attendance.getSessions()));
    }

    // تغيير حالة حضور طالب (مثل Present أو Late)
    @PutMapping("/{lectureId}/{studentId}")
    public ResponseEntity<String> updateAttendanceStatus(
            @PathVariable String lectureId,
            @PathVariable String studentId,
            @RequestBody Map<String, String> body) {
        AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, studentId);
        if (attendance == null) {
            return ResponseEntity.notFound().build();
        }
        String newStatus = body.get("status");
        if (!List.of("Present", "Late", "Absent", "Excuse").contains(newStatus)) {
            return ResponseEntity.badRequest().body("Invalid status. Use: Present, Late, Absent, Excuse");
        }
        attendance.setStatus(newStatus);
        attendanceRepository.save(attendance);
        return ResponseEntity.ok("Attendance status updated to " + newStatus);
    }

    // حذف كل سجلات الحضور لمحاضرة معينة
    @DeleteMapping("/{lectureId}")
    public ResponseEntity<String> deleteAttendance(@PathVariable String lectureId) {
        attendanceRepository.deleteByLectureId(lectureId);
        return ResponseEntity.ok("Attendance records for lecture " + lectureId + " deleted");
    }
}