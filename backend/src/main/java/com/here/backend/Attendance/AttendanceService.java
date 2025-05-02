package com.here.backend.Attendance;

import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Emails.EmailSenderService;
import com.here.backend.Lecture.LectureEntity;
import com.here.backend.Lecture.LectureRepository;
import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    public String determineStatus(String lectureId, String studentId, int lateThreshold) {
        logger.debug("Determining status for lectureId: {}, studentId: {}", lectureId, studentId);
        AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, studentId);
        if (attendance == null) {
            logger.warn("No attendance record found for lectureId: {}, studentId: {}", lectureId, studentId);
            return "Absent";
        }

        // Check if status is already set to a valid value
        String currentStatus = attendance.getStatus();
        if (currentStatus != null && List.of("Present", "Late", "Excuse").contains(currentStatus)) {
            logger.debug("Preserving existing status: {} for lectureId: {}, studentId: {}", currentStatus, lectureId, studentId);
            return currentStatus;
        }

        // Check sessions for automated determination
        if (attendance.getSessions() == null || attendance.getSessions().isEmpty()) {
            logger.debug("No sessions found for lectureId: {}, studentId: {}, returning Absent", lectureId, studentId);
            return "Absent";
        }

        Optional<LectureEntity> lectureOpt = lectureRepository.findByLectureId(lectureId);
        if (!lectureOpt.isPresent()) {
            logger.warn("No lecture found for lectureId: {}", lectureId);
            return "Absent";
        }

        LectureEntity lecture = lectureOpt.get();
        String courseId = lecture.getCourseId();
        Optional<CourseEntity> courseOpt = courseRepository.findByCourseIdAndLectureIdIsNull(courseId);
        if (!courseOpt.isPresent()) {
            logger.warn("No course found for courseId: {}", courseId);
            return "Absent";
        }

        CourseEntity course = courseOpt.get();
        LocalTime startTime;
        try {
            startTime = LocalTime.parse(lecture.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            logger.error("Invalid start time format for lectureId: {}: {}", lectureId, e.getMessage());
            return "Absent";
        }

        for (AttendanceEntity.SessionAttendance session : attendance.getSessions()) {
            if (!session.getFirstDetectionTime().equals("undetected")) {
                try {
                    LocalTime detectionTime = LocalTime.parse(session.getFirstDetectionTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
                    long minutesLate = java.time.Duration.between(startTime, detectionTime).toMinutes();
                    String status = minutesLate <= lateThreshold ? "Present" : "Late";
                    logger.debug("Determined status: {} based on detection time for lectureId: {}, studentId: {}, session: {}", 
                            status, lectureId, studentId, session.getSessionId());
                    return status;
                } catch (DateTimeParseException e) {
                    logger.warn("Invalid detection time format in session {}: {}", session.getSessionId(), session.getFirstDetectionTime());
                    continue;
                }
            }
        }

        logger.debug("All sessions undetected for lectureId: {}, studentId: {}, returning Absent", lectureId, studentId);
        return "Absent";
    }

    public void sendAbsenceEmail(String lectureId, String studentId) {
        Optional<StudentEntity> studentOpt = studentRepository.findById(studentId);
        if (!studentOpt.isPresent()) {
            logger.warn("Student not found for studentId: {}", studentId);
            return;
        }
        StudentEntity student = studentOpt.get();

        Optional<LectureEntity> lectureOpt = lectureRepository.findByLectureId(lectureId);
        String courseName = lectureOpt.isPresent() ? 
            courseRepository.findByCourseIdAndLectureIdIsNull(lectureOpt.get().getCourseId())
                .map(CourseEntity::getName)
                .orElse("Unknown Course") : 
            "Unknown Course";

        String emailSubject = "Absence Alert: " + courseName;
        String emailBody = "You were marked absent in " + courseName + " (Lecture ID: " + lectureId + ") on " + LocalDateTime.now().toString();
        try {
            emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
            logger.info("Absence email sent to student: {} for lectureId: {}", student.getEmail(), lectureId);
        } catch (Exception e) {
            logger.error("Failed to send absence email to student: {} for lectureId: {}: {}", 
                    student.getEmail(), lectureId, e.getMessage());
        }
    }

    public int countDetections(String lectureId, String studentId) {
        AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, studentId);
        if (attendance == null || attendance.getSessions() == null) {
            logger.debug("No detections found for lectureId: {}, studentId: {}", lectureId, studentId);
            return 0;
        }
        int detectionCount = (int) attendance.getSessions().stream()
                .filter(s -> !s.getFirstDetectionTime().equals("undetected"))
                .count();
        logger.debug("Detection count: {} for lectureId: {}, studentId: {}", detectionCount, lectureId, studentId);
        return detectionCount;
    }
}