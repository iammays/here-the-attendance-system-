//backend\src\main\java\com\here\backend\Attendance\AttendanceService.java

package com.here.backend.Attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private CourseRepository courseRepository;

    // معرفة إذا كان الطالب Present أو Late أو Absent بناءً على وقت الاكتشاف
    public String determineStatus(String lectureId, String studentId, int lateThreshold) {
        AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, studentId);
        if (attendance == null || attendance.getSessions().isEmpty()) return "Absent";

        CourseEntity course = courseRepository.findByCourseId(lectureId).orElseThrow();
        LocalDateTime startTime = LocalDateTime.parse(course.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
        
        boolean detectedInAnySession = attendance.getSessions().stream()
                .anyMatch(session -> !session.getDetectionTime().equals("undetected"));
        
        if (!detectedInAnySession) {
            return "Absent";
        }

        String firstDetection = attendance.getSessions().get(0).getDetectionTime();
        if (firstDetection.equals("undetected")) {
            return "Late";
        }

        LocalDateTime detectionTime = LocalDateTime.parse(firstDetection, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        long minutesLate = java.time.Duration.between(startTime, detectionTime).toMinutes();
        
        return minutesLate <= lateThreshold ? "Present" : "Late";
    }

    // عد عدد مرات ظهور الطالب في المحاضرة
    public int countDetections(String lectureId, String studentId) {
        AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, studentId);
        if (attendance == null) return 0;
        return (int) attendance.getSessions().stream()
                .filter(s -> !s.getDetectionTime().equals("undetected"))
                .count();
    }
}