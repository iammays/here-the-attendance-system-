// backend/src/main/java/com/here/backend/Attendance/AttendanceService.java
package com.here.backend.Attendance;

import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Emails.EmailSenderService;
import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    public String determineStatus(String lectureId, String studentId, int lateThreshold) {
        AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, studentId);
        if (attendance == null || attendance.getSessions() == null || attendance.getSessions().isEmpty()) {
            return "Absent";
        }

        Optional<CourseEntity> courseOpt = courseRepository.findByLectureId(lectureId);
        if (!courseOpt.isPresent()) {
            return "Absent";
        }

        CourseEntity course = courseOpt.get();
        LocalTime startTime = LocalTime.parse(course.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));

        for (AttendanceEntity.SessionAttendance session : attendance.getSessions()) {
            if (!session.getFirstDetectionTime().equals("undetected")) {
                try {
                    LocalTime detectionTime = LocalTime.parse(session.getFirstDetectionTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
                    long minutesLate = java.time.Duration.between(startTime, detectionTime).toMinutes();
                    return minutesLate <= lateThreshold ? "Present" : "Late";
                } catch (DateTimeParseException e) {
                    System.out.println("❌ Invalid detection time format in session " + session.getSessionId() + ": " + session.getFirstDetectionTime());
                    continue;
                }
            }
        }
        return "Absent";
    }

    // إرسال إيميل للطالب عند الغياب (يتم استدعاؤها فقط عند الـ finalize)
    public void sendAbsenceEmail(String lectureId, String studentId) {
        Optional<StudentEntity> student = studentRepository.findById(studentId);
        if (student.isPresent()) {
            Optional<CourseEntity> courseOpt = courseRepository.findByLectureId(lectureId);
            String courseName = courseOpt.isPresent() ? courseOpt.get().getName() : "Unknown Course";
            
            String emailSubject = "Absence Alert: " + courseName;
            String emailBody = "You were marked absent in " + courseName + " (Lecture ID: " + lectureId + ") on " + LocalDateTime.now().toString();
            emailSenderService.sendSimpleEmail(student.get().getEmail(), emailSubject, emailBody);
        }
    }

    // حساب عدد مرات الاكتشاف
    public int countDetections(String lectureId, String studentId) {
        AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, studentId);
        if (attendance == null || attendance.getSessions() == null) return 0;
        return (int) attendance.getSessions().stream()
                .filter(s -> !s.getFirstDetectionTime().equals("undetected"))
                .count();
    }
}