//backend\src\main\java\com\here\backend\Attendance\AttendanceController.java


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
    @Autowired
    private AttendanceService attendanceService;

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
        attendance.setStatus(record.getStatus());  // حفظ الـ status الجديد
        attendanceRepository.save(attendance);
        return ResponseEntity.ok("Attendance saved");
    }

    @GetMapping("/{lectureId}/{studentId}")
    public ResponseEntity<?> getAttendanceReport(@PathVariable String lectureId, @PathVariable String studentId, @RequestParam int lateThreshold) {
        String status = attendanceService.determineStatus(lectureId, studentId, lateThreshold);
        int detectionCount = attendanceService.countDetections(lectureId, studentId);
        AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, studentId);
        return ResponseEntity.ok(new AttendanceReport(status, detectionCount, attendance.getSessions()));
    }
}

class AttendanceRecord {
    private String lectureId;
    private int sessionId;
    private String studentId;
    private String detectionTime;
    private String screenshotPath;
    private String status;  // حقل جديد للـ status

    public String getLectureId() { return lectureId; }
    public void setLectureId(String lectureId) { this.lectureId = lectureId; }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getDetectionTime() { return detectionTime; }
    public void setDetectionTime(String detectionTime) { this.detectionTime = detectionTime; }
    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

class AttendanceReport {
    private String status;
    private int detectionCount;
    private List<AttendanceEntity.SessionAttendance> sessions;

    public AttendanceReport(String status, int detectionCount, List<AttendanceEntity.SessionAttendance> sessions) {
        this.status = status;
        this.detectionCount = detectionCount;
        this.sessions = sessions;
    }

    public String getStatus() { return status; }
    public int getDetectionCount() { return detectionCount; }
    public List<AttendanceEntity.SessionAttendance> getSessions() { return sessions; }
}