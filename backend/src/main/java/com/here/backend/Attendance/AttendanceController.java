package com.here.backend.Attendance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Emails.EmailSenderService;
import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;
import com.here.backend.Teacher.TeacherEntity;
import com.here.backend.Teacher.TeacherRepository;
import com.here.backend.Lecture.LectureEntity;
import com.here.backend.Lecture.LectureRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private WfStatusService wfStatusService;

    @Autowired
    private LectureRepository lectureRepository;

    @PostMapping
    public ResponseEntity<String> saveAttendance(@RequestBody AttendanceRecord record) {
        String lectureId = record.getLectureId();
        String courseId = record.getCourseId();

        if (lectureId == null || lectureId.isEmpty()) {
            if (courseId == null || courseId.isEmpty()) {
                return ResponseEntity.badRequest().body("courseId is required when lectureId is not provided");
            }
            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            String dateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String timeStr = currentTime.format(DateTimeFormatter.ofPattern("HHmm"));
            lectureId = courseId + "-" + dateStr + "-" + timeStr;

            if (lectureRepository.findByLectureId(lectureId).isPresent()) {
                int counter = 1;
                String newLectureId = lectureId;
                while (lectureRepository.findByLectureId(newLectureId).isPresent()) {
                    newLectureId = lectureId + "_" + counter;
                    counter++;
                }
                lectureId = newLectureId;
            }
        }

        CourseEntity course = courseRepository.findByCourseIdAndLectureIdIsNull(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found for courseId: " + courseId));

        AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, record.getStudentId());

        if (attendance == null) {
            attendance = new AttendanceEntity();
            attendance.setAttendanceId(UUID.randomUUID().toString());
            attendance.setLectureId(lectureId);
            attendance.setStudentId(record.getStudentId());
            attendance.setCourseId(courseId);
            attendance.setStudentName(getStudentName(record.getStudentId()));
            attendance.setSessions(new ArrayList<>());
            attendance.setFirstCheckTimes(new ArrayList<>());
            attendance.setFirstDetectedAt(record.getFirstDetectedAt() != null ? record.getFirstDetectedAt() : "undetected");
        }

        if (record.getSessions() != null && !record.getSessions().isEmpty()) {
            for (AttendanceEntity.SessionAttendance session : record.getSessions()) {
                boolean sessionExists = attendance.getSessions().stream()
                        .anyMatch(s -> s.getSessionId() == session.getSessionId());
                if (!sessionExists) {
                    attendance.getSessions().add(new AttendanceEntity.SessionAttendance(
                            session.getSessionId(),
                            session.getFirstDetectionTime() != null ? session.getFirstDetectionTime() : "undetected"
                    ));
                }
            }
        } else if (record.getSessionId() >= 0 && record.getDetectionTime() != null) {
            boolean sessionExists = attendance.getSessions().stream()
                    .anyMatch(s -> s.getSessionId() == record.getSessionId());
            if (!sessionExists) {
                attendance.getSessions().add(new AttendanceEntity.SessionAttendance(
                        record.getSessionId(),
                        record.getDetectionTime() != null ? record.getDetectionTime() : "undetected"
                ));
            }
        } else {
            int sessionId = 0;
            String detectionTime = "undetected";
            attendance.getSessions().add(new AttendanceEntity.SessionAttendance(sessionId, detectionTime));
        }

        if (record.getFirstCheckTimes() != null && !record.getFirstCheckTimes().isEmpty()) {
            for (AttendanceRecord.FirstCheckTime checkTime : record.getFirstCheckTimes()) {
                boolean checkTimeExists = attendance.getFirstCheckTimes().stream()
                        .anyMatch(c -> c.getSessionId() == checkTime.getSessionId());
                if (!checkTimeExists) {
                    attendance.getFirstCheckTimes().add(new AttendanceEntity.FirstCheckTime(
                            checkTime.getSessionId(),
                            checkTime.getFirstCheckTime() != null ? checkTime.getFirstCheckTime() : "undetected"
                    ));
                }
            }
        }

        attendance.setStatus(record.getStatus());
        attendanceRepository.save(attendance);

        if ("Absent".equalsIgnoreCase(attendance.getStatus())) {
            String studentId = attendance.getStudentId();
            StudentEntity student = studentRepository.findById(studentId).orElse(null);
            if (student != null) {
                int currentAbsences = student.getCourseAbsences().getOrDefault(courseId, 0);
                int newAbsences = currentAbsences + 1;
                student.getCourseAbsences().put(courseId, newAbsences);
                studentRepository.save(student);

                String courseName = course.getName();
                String teacherId = course.getTeacherId();
                String teacherEmail = teacherRepository.findById(teacherId).map(TeacherEntity::getEmail).orElse(null);
                String advisorId = student.getAdvisor();
                String advisorEmail = teacherRepository.findById(advisorId).map(TeacherEntity::getEmail).orElse(null);

                String emailSubject = "Absence Alert: " + courseName;
                String emailBody = "Dear " + student.getName() + ",\nAn absence has been recorded for you in the course " + courseName + ".";

                try {
                    if (student.getEmail() != null) {
                        emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
                    }
                    if (teacherEmail != null) {
                        emailSenderService.sendSimpleEmail(teacherEmail, emailSubject,
                                "Absence recorded for " + student.getName() + " in course " + courseName);
                    }
                    if (advisorEmail != null) {
                        emailSenderService.sendSimpleEmail(advisorEmail, emailSubject, emailBody);
                    }
                } catch (Exception e) {
                    logger.error("Failed to send absence email for student {}: {}", studentId, e.getMessage());
                }

                try {
                    wfStatusService.checkWfStatus(studentId, courseId);
                } catch (Exception e) {
                    logger.error("Error checking WF status for student {}: {}", studentId, e.getMessage());
                }
            }
        }

        return ResponseEntity.ok("Attendance saved with lectureId: " + lectureId);
    }

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
        logger.info("Updated attendance status to {} for lectureId: {}, studentId: {}", newStatus, lectureId, studentId);
        return ResponseEntity.ok("Attendance status updated to " + newStatus);
    }

    @PutMapping("/updateWithEmail/{lectureId}/{studentId}")
    public ResponseEntity<String> updateAttendanceStatusWithEmail(
            @PathVariable String lectureId,
            @PathVariable String studentId,
            @RequestBody Map<String, String> body) {
        try {
            AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, studentId);
            if (attendance == null) {
                return ResponseEntity.status(404).body("Attendance record not found for lecture " + lectureId + " and student " + studentId);
            }

            String newStatus = body.get("status");
            if (!List.of("Present", "Late", "Absent", "Excuse").contains(newStatus)) {
                return ResponseEntity.badRequest().body("Invalid status. Use: Present, Late, Absent, Excuse");
            }

            String oldStatus = attendance.getStatus();
            attendance.setStatus(newStatus);
            attendanceRepository.save(attendance);
            logger.info("Updated attendance status with email to {} from {} for lectureId: {}, studentId: {}", 
                    newStatus, oldStatus, lectureId, studentId);

            StudentEntity student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                logger.error("Student not found: {}", studentId);
                return ResponseEntity.ok("Attendance status updated to " + newStatus + ", but student not found for absence tracking");
            }

            String courseId = attendance.getCourseId();
            String courseName = courseRepository.findByCourseIdAndLectureIdIsNull(courseId)
                    .map(CourseEntity::getName)
                    .orElse("Course Not Found");

            Map<String, Integer> courseAbsences = student.getCourseAbsences();
            int currentAbsences = courseAbsences.getOrDefault(courseId, 0);

            if ("Absent".equalsIgnoreCase(newStatus) && !"Absent".equalsIgnoreCase(oldStatus)) {
                int newAbsences = currentAbsences + 1;
                courseAbsences.put(courseId, newAbsences);
                studentRepository.save(student);

                String teacherId = courseRepository.findByCourseIdAndLectureIdIsNull(courseId)
                        .map(CourseEntity::getTeacherId)
                        .orElse(null);
                String teacherEmail = teacherId != null ?
                        teacherRepository.findById(teacherId).map(TeacherEntity::getEmail).orElse(null) : null;
                String advisorId = student.getAdvisor();
                String advisorEmail = teacherRepository.findById(advisorId).map(TeacherEntity::getEmail).orElse(null);

                String emailSubject = "Absence Alert: " + courseName;
                String emailBody = "Dear " + student.getName() + ",\nAn absence has been recorded for you in the course " + courseName + ".";

                try {
                    if (student.getEmail() != null) {
                        emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
                        logger.info("Absence email sent to student: {}", student.getEmail());
                    }
                    if (teacherEmail != null) {
                        emailSenderService.sendSimpleEmail(teacherEmail, emailSubject,
                                "Absence recorded for " + student.getName() + " in course " + courseName);
                        logger.info("Absence email sent to teacher: {}", teacherEmail);
                    }
                    if (advisorEmail != null) {
                        emailSenderService.sendSimpleEmail(advisorEmail, emailSubject, emailBody);
                        logger.info("Absence email sent to advisor: {}", advisorEmail);
                    }
                } catch (Exception e) {
                    logger.error("Failed to send absence email for student {}: {}", studentId, e.getMessage());
                }

                try {
                    wfStatusService.checkWfStatus(studentId, courseId);
                } catch (Exception e) {
                    logger.error("Error checking WF status for student {}: {}", studentId, e.getMessage());
                }
            } else if (!"Absent".equalsIgnoreCase(newStatus) && "Absent".equalsIgnoreCase(oldStatus)) {
                int newAbsences = Math.max(0, currentAbsences - 1);
                courseAbsences.put(courseId, newAbsences);
                studentRepository.save(student);
            }

            return ResponseEntity.ok("Attendance status updated to " + newStatus);
        } catch (Exception e) {
            logger.error("Error updating attendance status for lectureId: {}, studentId: {}: {}", lectureId, studentId, e.getMessage());
            return ResponseEntity.status(500).body("Error updating attendance status: " + e.getMessage());
        }
    }

    @DeleteMapping("/{lectureId}")
    public ResponseEntity<String> deleteAttendance(@PathVariable String lectureId) {
        attendanceRepository.deleteByLectureId(lectureId);
        logger.info("Deleted attendance records for lectureId: {}", lectureId);
        return ResponseEntity.ok("Attendance records for lecture " + lectureId + " deleted");
    }

    @DeleteMapping("/lecture/{lectureId}")
    public ResponseEntity<String> deleteLectureAndAttendance(@PathVariable String lectureId) {
        attendanceRepository.deleteByLectureId(lectureId);
        Optional<LectureEntity> lectureOpt = lectureRepository.findByLectureId(lectureId);
        if (lectureOpt.isPresent()) {
            lectureRepository.delete(lectureOpt.get());
            logger.info("Deleted lecture and attendance records for lectureId: {}", lectureId);
            return ResponseEntity.ok("Lecture " + lectureId + " and its attendance records deleted");
        } else {
            logger.warn("Lecture not found for lectureId: {}", lectureId);
            return ResponseEntity.badRequest().body("Lecture not found for lectureId: " + lectureId);
        }
    }

    @GetMapping("/table/{lectureId}")
    public ResponseEntity<List<Map<String, Object>>> getAttendanceTable(
            @PathVariable String lectureId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "studentId") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        logger.info("Processing attendance table request for lectureId: {}", lectureId);

        // Parse lectureId (e.g., SWER-345-2025-04-20-1430)
        String[] lectureParts = lectureId.split("-");
        if (lectureParts.length < 4) { // Minimum: courseId (1+ parts), YYYY, MM, DD, HHmm
            logger.error("Invalid lectureId format: {}. Expected at least 4 parts (e.g., SWER-345-2025-04-20-1430)", lectureId);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        // Reconstruct courseId, date, and startTime
        String rawStartTime = lectureParts[lectureParts.length - 1]; // Last part is HHmm
        String date = lectureParts[lectureParts.length - 4] + "-" + lectureParts[lectureParts.length - 3] + "-" + lectureParts[lectureParts.length - 2]; // YYYY-MM-DD
        String courseId = String.join("-", Arrays.copyOfRange(lectureParts, 0, lectureParts.length - 4)); // All parts before date

        // Validate startTime
        if (!rawStartTime.matches("\\d{4}")) {
            logger.error("Invalid startTime format in lectureId: {}. Expected HHmm (e.g., 1430)", rawStartTime);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        String startTime = rawStartTime.substring(0, 2) + ":" + rawStartTime.substring(2);
        logger.debug("Parsed lectureId: courseId={}, date={}, startTime={}", courseId, date, startTime);

        // Validate date
        try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format in lectureId: {}. Expected YYYY-MM-DD", date);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        // Find course template
        Optional<CourseEntity> courseOpt = courseRepository.findByCourseIdAndLectureIdIsNull(courseId);
        if (!courseOpt.isPresent()) {
            logger.error("Course not found for courseId: {}", courseId);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        CourseEntity course = courseOpt.get();
        logger.info("Found course for courseId: {}", courseId);

        // Check or create lecture
        Optional<LectureEntity> lectureOpt = lectureRepository.findByLectureId(lectureId);
        LectureEntity lecture;
        if (!lectureOpt.isPresent()) {
            logger.info("Lecture not found for lectureId: {}. Creating new lecture.", lectureId);
            lecture = new LectureEntity();
            lecture.setCourseId(courseId);
            lecture.setLectureId(lectureId);
            lecture.setName(course.getName());
            lecture.setRoomId(course.getRoomId());
            lecture.setTeacherId(course.getTeacherId());
            lecture.setStartTime(startTime);
            lecture.setEndTime(course.getEndTime());
            lecture.setDay(LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).getDayOfWeek().name().toUpperCase());
            lecture.setCategory(course.getCategory());
            lecture.setCredits(course.getCredits());
            lecture.setLateThreshold(course.getLateThreshold());
            try {
                lectureRepository.save(lecture);
                logger.info("Successfully saved lecture with lectureId: {}", lectureId);
            } catch (Exception e) {
                logger.error("Error saving lecture with lectureId: {}: {}", lectureId, e.getMessage(), e);
                return ResponseEntity.status(500).body(Collections.emptyList());
            }
        } else {
            lecture = lectureOpt.get();
            logger.info("Found existing lecture with lectureId: {}", lectureId);
        }

        // Fetch or initialize attendance records
        List<AttendanceEntity> attendances = attendanceRepository.findByLectureId(lectureId);
        if (attendances.isEmpty()) {
            logger.info("No attendance records found for lectureId: {}. Initializing table.", lectureId);
            ResponseEntity<String> initResponse = initializeAttendanceTable(lectureId);
            if (!initResponse.getStatusCode().is2xxSuccessful()) {
                logger.error("Failed to initialize attendance table for lectureId: {}", lectureId);
                return ResponseEntity.status(500).body(Collections.emptyList());
            }
            attendances = attendanceRepository.findByLectureId(lectureId);
            if (attendances.isEmpty()) {
                logger.error("No students enrolled in course: {}", courseId);
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }
        }

        List<Map<String, Object>> table = new ArrayList<>();
        for (AttendanceEntity attendance : attendances) {
            Map<String, Object> row = new HashMap<>();
            String studentId = attendance.getStudentId();

            row.put("studentId", studentId);
            row.put("courseId", attendance.getCourseId());
            row.put("status", attendance.getStatus() != null ? attendance.getStatus() : "Pending");

            String studentName = studentRepository.findById(studentId)
                    .map(StudentEntity::getName)
                    .orElse("Unknown");
            row.put("studentName", studentName);

            List<Map<String, Object>> sessions = attendance.getSessions() != null ?
                    attendance.getSessions().stream()
                            .map(s -> {
                                Map<String, Object> sessionInfo = new HashMap<>();
                                sessionInfo.put("sessionId", s.getSessionId());
                                sessionInfo.put("firstDetectionTime", s.getFirstDetectionTime() != null ?
                                        s.getFirstDetectionTime() : "undetected");
                                return sessionInfo;
                            })
                            .collect(Collectors.toList()) :
                    new ArrayList<>();
            row.put("sessions", sessions);

            int totalSessions = attendance.getSessions() != null ? attendance.getSessions().size() : 0;
            long detectionCount = attendance.getSessions() != null ?
                    attendance.getSessions().stream()
                            .filter(s -> s.getFirstDetectionTime() != null && !s.getFirstDetectionTime().equals("undetected"))
                            .count() : 0;
            row.put("totalSessions", totalSessions);
            row.put("detectionCount", detectionCount);

            List<String> firstCheckTimes = attendance.getFirstCheckTimes() != null ?
                    attendance.getFirstCheckTimes().stream()
                            .map(AttendanceEntity.FirstCheckTime::getFirstCheckTime)
                            .collect(Collectors.toList()) :
                    new ArrayList<>();
            row.put("firstCheckTimes", firstCheckTimes);

            String firstDetectedAt = sessions.stream()
                    .filter(s -> !s.get("firstDetectionTime").equals("undetected"))
                    .findFirst()
                    .map(s -> s.get("firstDetectionTime").toString())
                    .orElse("undetected");
            row.put("firstDetectedAt", firstDetectedAt);

            table.add(row);
        }

        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            table = table.stream()
                    .filter(row -> ((String) row.get("studentId")).toLowerCase().contains(searchLower) ||
                            ((String) row.get("studentName")).toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        Comparator<Map<String, Object>> comparator;
        switch (sortBy) {
            case "status":
                comparator = Comparator.comparing(row -> (String) row.get("status"), Comparator.nullsLast(String::compareTo));
                break;
            case "studentName":
                comparator = Comparator.comparing(row -> (String) row.get("studentName"));
                break;
            case "detectionCount":
                comparator = Comparator.comparing(row -> (Long) row.get("detectionCount"));
                break;
            default:
                comparator = Comparator.comparing(row -> (String) row.get("studentId"));
        }
        if ("desc".equals(sortOrder)) comparator = comparator.reversed();
        table.sort(comparator);

        logger.info("Returning attendance table with {} rows for lectureId: {}", table.size(), lectureId);
        return ResponseEntity.ok(table);
    }

    @PostMapping("/approve-wf")
    public ResponseEntity<String> approveWf(@RequestBody Map<String, String> request) {
        String studentId = request.get("studentId");
        String courseId = request.get("courseId");

        if (studentId == null || courseId == null) {
            return ResponseEntity.badRequest().body("Missing studentId or courseId in the request body.");
        }

        return studentRepository.findById(studentId).map(student -> {
            Map<String, String> wfStatus = student.getCourseWfStatus();
            wfStatus.put(courseId, "Approved");
            studentRepository.save(student);

            String teacherId = courseRepository.findByCourseIdAndLectureIdIsNull(courseId)
                    .map(CourseEntity::getTeacherId)
                    .orElse(null);
            String teacherEmail = teacherId != null ?
                    teacherRepository.findById(teacherId).map(TeacherEntity::getEmail).orElse(null) : null;
            String advisorId = student.getAdvisor();
            String advisorEmail = teacherRepository.findById(advisorId).map(TeacherEntity::getEmail).orElse(null);

            String emailSubject = "WF Approved: " + courseId;
            String emailBody = "Your WF status for course " + courseId + " has been approved.";

            try {
                if (student.getEmail() != null) {
                    emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
                    logger.info("WF status email sent to student: {}", student.getEmail());
                }
                if (teacherEmail != null) {
                    emailSenderService.sendSimpleEmail(teacherEmail, emailSubject,
                            "You approved WF for " + student.getName() + " in course " + courseId);
                    logger.info("WF status email sent to teacher: {}", teacherEmail);
                }
                if (advisorEmail != null) {
                    emailSenderService.sendSimpleEmail(advisorEmail, emailSubject, emailBody);
                    logger.info("WF status email sent to advisor: {}", advisorEmail);
                }
            } catch (Exception e) {
                logger.error("Failed to send WF email: {}", e.getMessage());
            }

            return ResponseEntity.ok("WF status set to Approved for course " + courseId);
        }).orElse(ResponseEntity.badRequest().body("Student not found."));
    }

    @PostMapping("/ignore-wf")
    public ResponseEntity<String> ignoreWf(@RequestBody Map<String, String> request) {
        String studentId = request.get("studentId");
        String courseId = request.get("courseId");

        if (studentId == null || courseId == null) {
            return ResponseEntity.badRequest().body("Missing studentId or courseId in the request body.");
        }

        return studentRepository.findById(studentId).map(student -> {
            Map<String, String> wfStatus = student.getCourseWfStatus();
            wfStatus.put(courseId, "Ignored");
            studentRepository.save(student);

            String teacherId = courseRepository.findByCourseIdAndLectureIdIsNull(courseId)
                    .map(CourseEntity::getTeacherId)
                    .orElse(null);
            String teacherEmail = teacherId != null ?
                    teacherRepository.findById(teacherId).map(TeacherEntity::getEmail).orElse(null) : null;
            String advisorId = student.getAdvisor();
            String advisorEmail = teacherRepository.findById(advisorId).map(TeacherEntity::getEmail).orElse(null);

            String emailSubject = "WF Ignored: " + courseId;
            String emailBody = "Your WF request for course " + courseId + " has been ignored.";

            try {
                if (student.getEmail() != null) {
                    emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
                    logger.info("WF ignore email sent to student: {}", student.getEmail());
                }
                if (teacherEmail != null) {
                    emailSenderService.sendSimpleEmail(teacherEmail, emailSubject,
                            "You ignored WF for " + student.getName() + " in course " + courseId);
                    logger.info("WF ignore email sent to teacher: {}", teacherEmail);
                }
                if (advisorEmail != null) {
                    emailSenderService.sendSimpleEmail(advisorEmail, emailSubject, emailBody);
                    logger.info("WF ignore email sent to advisor: {}", advisorEmail);
                }
            } catch (Exception e) {
                logger.error("Failed to send WF ignore email: {}", e.getMessage());
            }

            return ResponseEntity.ok("WF status set to Ignored for course " + courseId);
        }).orElse(ResponseEntity.badRequest().body("Student not found."));
    }

    @PostMapping("/check-wf-status")
    public ResponseEntity<String> checkWfStatus(@RequestParam String studentId, @RequestParam String courseId) {
        return studentRepository.findById(studentId).map(student -> {
            Map<String, String> wfStatus = student.getCourseWfStatus();
            String status = wfStatus.getOrDefault(courseId, "Pending");

            String teacherId = courseRepository.findByCourseIdAndLectureIdIsNull(courseId)
                    .map(CourseEntity::getTeacherId)
                    .orElse(null);
            String teacherEmail = teacherId != null ?
                    teacherRepository.findById(teacherId).map(TeacherEntity::getEmail).orElse(null) : null;
            String advisorId = student.getAdvisor();
            String advisorEmail = teacherRepository.findById(advisorId).map(TeacherEntity::getEmail).orElse(null);

            if ("Approved".equalsIgnoreCase(status)) {
                String emailSubject = "WF Approved: " + courseId;
                String emailBody = "Your WF status for course " + courseId + " has been approved due to excessive absences.";

                try {
                    if (student.getEmail() != null) {
                        emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
                        logger.info("WF email sent to student: {}", student.getEmail());
                    }
                    if (teacherEmail != null) {
                        emailSenderService.sendSimpleEmail(teacherEmail, emailSubject,
                                "You approved WF for " + student.getName() + " in course " + courseId);
                        logger.info("WF email sent to teacher: {}", teacherEmail);
                    }
                    if (advisorEmail != null) {
                        emailSenderService.sendSimpleEmail(advisorEmail, emailSubject, emailBody);
                        logger.info("WF email sent to advisor: {}", advisorEmail);
                    }
                } catch (Exception e) {
                    logger.error("Failed to send WF email: {}", e.getMessage());
                }
                return ResponseEntity.ok("WF is approved for course " + courseId);
            } else {
                return ResponseEntity.ok("WF is not approved for course " + courseId);
            }
        }).orElse(ResponseEntity.badRequest().body("Student not found."));
    }

    @PostMapping("/finalize/{lectureId}")
    public ResponseEntity<String> finalizeAttendance(@PathVariable String lectureId) {
        logger.info("Finalizing attendance for lectureId: {}", lectureId);
        List<AttendanceEntity> attendances = attendanceRepository.findByLectureId(lectureId);
        if (attendances == null || attendances.isEmpty()) {
            logger.warn("No attendance records found for lectureId: {}", lectureId);
            return ResponseEntity.badRequest().body("No attendance records found for lecture " + lectureId);
        }

        for (AttendanceEntity attendance : attendances) {
            try {
                String currentStatus = attendance.getStatus();
                String finalStatus = attendanceService.determineStatus(lectureId, attendance.getStudentId(), 5);
                logger.debug("Student: {}, Current status: {}, Determined status: {}", 
                        attendance.getStudentId(), currentStatus, finalStatus);

                if (!finalStatus.equals(currentStatus)) {
                    if (!"Absent".equals(currentStatus) && "Absent".equals(finalStatus)) {
                        attendanceService.sendAbsenceEmail(lectureId, attendance.getStudentId());
                    }
                    attendance.setStatus(finalStatus);
                    attendanceRepository.save(attendance);
                    logger.info("Updated status to {} for student: {} in lectureId: {}", 
                            finalStatus, attendance.getStudentId(), lectureId);
                } else {
                    logger.debug("No status change needed for student: {} in lectureId: {}", 
                            attendance.getStudentId(), lectureId);
                }
            } catch (Exception e) {
                logger.error("Error processing attendance for student {}: {}", attendance.getStudentId(), e.getMessage());
                return ResponseEntity.status(500).body("Error finalizing attendance: " + e.getMessage());
            }
        }

        List<Map<String, Object>> attendanceTable = getAttendanceTable(lectureId, null, "studentId", "asc").getBody();
        StringBuilder report = new StringBuilder("Attendance Report for Lecture " + lectureId + ":\n");
        for (Map<String, Object> row : attendanceTable) {
            report.append(String.format("Student: %s (%s), Status: %s, Detection Count: %d\n",
                    row.get("studentName"), row.get("studentId"), row.get("status"), row.get("detectionCount")));
        }
        logger.info(report.toString());

        return ResponseEntity.ok("Attendance finalized and report generated for lecture " + lectureId);
    }

    @PostMapping("/addNewDay")
    public ResponseEntity<Map<String, Object>> addNewDay(@RequestBody Map<String, String> request) {
        logger.info("Received request to add new day");
        String courseId = request.get("courseId");
        String date = request.get("date");
        String startTime = request.get("startTime");
        String endTime = request.get("endTime");
        String roomId = request.getOrDefault("roomId", "");

        // Validation
        if (courseId == null || date == null || startTime == null || endTime == null) {
            logger.error("Missing required fields: courseId, date, startTime, or endTime");
            return ResponseEntity.badRequest().body(Map.of("error", "يجب إدخال معرف الكورس، التاريخ، وقت البداية، وقت النهاية"));
        }

        // Validate date and time formats
        LocalDate parsedDate;
        LocalTime parsedStartTime;
        LocalTime parsedEndTime;
        try {
            parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            parsedStartTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
            parsedEndTime = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
            if (parsedEndTime.isBefore(parsedStartTime) || parsedEndTime.equals(parsedStartTime)) {
                logger.error("End time must be after start time: startTime={}, endTime={}", startTime, endTime);
                return ResponseEntity.badRequest().body(Map.of("error", "وقت النهاية يجب أن يكون بعد وقت البداية"));
            }
        } catch (DateTimeParseException e) {
            logger.error("Invalid date or time format: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "صيغة التاريخ أو الوقت غير صحيحة. استخدم YYYY-MM-DD للتاريخ و HH:mm للوقت"));
        }

        // Derive day from date
        String normalizedDay = parsedDate.getDayOfWeek().name().toUpperCase();
        logger.debug("Derived day from date {}: {}", date, normalizedDay);

        // Check if course exists
        Optional<CourseEntity> courseOpt = courseRepository.findByCourseIdAndLectureIdIsNull(courseId);
        if (!courseOpt.isPresent()) {
            logger.error("Course not found: {}", courseId);
            return ResponseEntity.badRequest().body(Map.of("error", "الكورس غير موجود"));
        }
        CourseEntity course = courseOpt.get();

        // Generate lectureId
        String lectureId = courseId + "-" + date + "-" + startTime.replace(":", "");
        logger.debug("Generated lectureId: {}", lectureId);

        // Check for existing lecture
        Optional<LectureEntity> existingLecture = lectureRepository.findByLectureId(lectureId);
        if (existingLecture.isPresent()) {
            logger.info("Lecture already exists: {}", lectureId);
            return ResponseEntity.ok(Map.of(
                "lectureId", lectureId,
                "message", "المحاضرة موجودة مسبقًا"
            ));
        }

        // Check for time conflicts
        List<LectureEntity> existingLectures = lectureRepository.findAll().stream()
                .filter(l -> l.getCourseId().equals(courseId) && l.getDay().equalsIgnoreCase(normalizedDay))
                .collect(Collectors.toList());

        for (LectureEntity existing : existingLectures) {
            try {
                LocalTime existingStart = LocalTime.parse(existing.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime existingEnd = LocalTime.parse(existing.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));

                if (!(parsedEndTime.isBefore(existingStart) || parsedStartTime.isAfter(existingEnd))) {
                    logger.error("Time conflict with existing lecture: {}", existing.getLectureId());
                    return ResponseEntity.badRequest().body(Map.of("error", "يوجد تعارض في الوقت مع محاضرة أخرى لنفس الكورس في نفس اليوم"));
                }
            } catch (DateTimeParseException e) {
                logger.warn("Skipping lecture with invalid time format: {}", existing.getLectureId());
                continue;
            }
        }

        // Create new lecture
        LectureEntity newLecture = new LectureEntity();
        newLecture.setCourseId(courseId);
        newLecture.setLectureId(lectureId);
        newLecture.setName(course.getName());
        newLecture.setRoomId(roomId.isEmpty() ? course.getRoomId() : roomId);
        newLecture.setTeacherId(course.getTeacherId());
        newLecture.setStartTime(startTime);
        newLecture.setEndTime(endTime);
        newLecture.setDay(normalizedDay);
        newLecture.setCategory(course.getCategory());
        newLecture.setCredits(course.getCredits());
        newLecture.setLateThreshold(course.getLateThreshold());

        try {
            lectureRepository.save(newLecture);
            logger.info("Lecture saved successfully: {}", lectureId);
        } catch (Exception e) {
            logger.error("Error saving lecture: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "فشل في حفظ المحاضرة: " + e.getMessage()));
        }

        // Initialize attendance table
        ResponseEntity<String> initResponse = initializeAttendanceTable(lectureId);
        if (!initResponse.getStatusCode().is2xxSuccessful()) {
            logger.error("Failed to initialize attendance table for lecture: {}", lectureId);
            lectureRepository.delete(newLecture);
            return ResponseEntity.status(initResponse.getStatusCode()).body(Map.of("error", initResponse.getBody()));
        }

        // Return JSON response
        Map<String, Object> response = new HashMap<>();
        response.put("lectureId", lectureId);
        response.put("message", "تم إنشاء المحاضرة بنجاح");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addNewLecture")
    public ResponseEntity<Map<String, Object>> addNewLecture(@RequestBody Map<String, String> request) {
        logger.info("Received request to add new lecture: {}", request);
        String courseId = request.get("courseId");
        String date = request.get("date");
        String startTime = request.get("startTime");
        String endTime = request.get("endTime");
        String day = request.get("day");
        String roomId = request.getOrDefault("roomId", "");

        // Validation
        if (courseId == null || date == null || startTime == null || endTime == null) {
            logger.error("Missing required fields: courseId, date, startTime, or endTime");
            return ResponseEntity.badRequest().body(Map.of("error", "Course ID, date, start time, and end time are required"));
        }

        // Validate date and time formats
        LocalDate parsedDate;
        LocalTime parsedStartTime;
        LocalTime parsedEndTime;
        try {
            parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            parsedStartTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
            parsedEndTime = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
            if (parsedEndTime.isBefore(parsedStartTime) || parsedEndTime.equals(parsedStartTime)) {
                logger.error("End time must be after start time: startTime={}, endTime={}", startTime, endTime);
                return ResponseEntity.badRequest().body(Map.of("error", "End time must be after start time"));
            }
        } catch (DateTimeParseException e) {
            logger.error("Invalid date or time format: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date or time format. Use YYYY-MM-DD for date and HH:mm for time"));
        }

        // Derive day from date
        String normalizedDay = parsedDate.getDayOfWeek().name().toUpperCase();
        logger.debug("Derived day from date {}: {}, sent day: {}", date, normalizedDay, day);

        // Check if course exists
        Optional<CourseEntity> courseOpt = courseRepository.findByCourseIdAndLectureIdIsNull(courseId);
        if (!courseOpt.isPresent()) {
            logger.error("Course not found: {}", courseId);
            return ResponseEntity.badRequest().body(Map.of("error", "Course not found"));
        }
        CourseEntity course = courseOpt.get();

        // Generate lectureId
        String lectureId = courseId + "-" + date + "-" + startTime.replace(":", "");
        logger.debug("Generated lectureId: {}", lectureId);

        // Check for existing lecture
        Optional<LectureEntity> existingLecture = lectureRepository.findByLectureId(lectureId);
        if (existingLecture.isPresent()) {
            logger.info("Lecture already exists: {}", lectureId);
            return ResponseEntity.ok(Map.of(
                "lectureId", lectureId,
                "message", "Lecture already exists"
            ));
        }

        // Check for time conflicts on the same date
        List<LectureEntity> existingLectures = lectureRepository.findAll().stream()
                .filter(l -> l.getCourseId().equals(courseId) && l.getLectureId().contains(date))
                .collect(Collectors.toList());

        for (LectureEntity existing : existingLectures) {
            try {
                LocalTime existingStart = LocalTime.parse(existing.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime existingEnd = LocalTime.parse(existing.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));

                if (!(parsedEndTime.isBefore(existingStart) || parsedStartTime.isAfter(existingEnd))) {
                    logger.error("Time conflict with existing lecture: {}", existing.getLectureId());
                    return ResponseEntity.badRequest().body(Map.of("error", "Time conflict with another lecture for the same course on the same date"));
                }
            } catch (DateTimeParseException e) {
                logger.warn("Skipping lecture with invalid time format: {}", existing.getLectureId());
                continue;
            }
        }

        // Create new lecture
        LectureEntity newLecture = new LectureEntity();
        newLecture.setCourseId(courseId);
        newLecture.setLectureId(lectureId);
        newLecture.setName(course.getName());
        newLecture.setRoomId(roomId.isEmpty() ? course.getRoomId() : roomId);
        newLecture.setTeacherId(course.getTeacherId());
        newLecture.setStartTime(startTime);
        newLecture.setEndTime(endTime);
        newLecture.setDay(normalizedDay);
        newLecture.setCategory(course.getCategory());
        newLecture.setCredits(course.getCredits());
        newLecture.setLateThreshold(course.getLateThreshold());

        try {
            lectureRepository.save(newLecture);
            logger.info("Lecture saved successfully: {}", lectureId);
        } catch (Exception e) {
            logger.error("Error saving lecture: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to save lecture: " + e.getMessage()));
        }

        // Initialize attendance table
        ResponseEntity<String> initResponse = initializeAttendanceTable(lectureId);
        if (!initResponse.getStatusCode().is2xxSuccessful()) {
            logger.error("Failed to initialize attendance table for lecture: {}", lectureId);
            lectureRepository.delete(newLecture);
            return ResponseEntity.status(initResponse.getStatusCode()).body(Map.of("error", initResponse.getBody()));
        }

        // Return JSON response
        Map<String, Object> response = new HashMap<>();
        response.put("lectureId", lectureId);
        response.put("message", "Lecture created successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/initialize/{lectureId}")
    public ResponseEntity<String> initializeAttendanceTable(@PathVariable String lectureId) {
        Optional<LectureEntity> lectureOpt = lectureRepository.findByLectureId(lectureId);
        if (!lectureOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Lecture not found for lectureId: " + lectureId);
        }
        LectureEntity lecture = lectureOpt.get();
        String courseId = lecture.getCourseId();

        List<StudentEntity> students = studentRepository.findByCourseId(courseId);
        if (students.isEmpty()) {
            return ResponseEntity.badRequest().body("No students enrolled in course: " + courseId);
        }

        for (StudentEntity student : students) {
            AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, student.getStudentId());
            if (attendance == null) {
                attendance = new AttendanceEntity();
                attendance.setAttendanceId(UUID.randomUUID().toString());
                attendance.setLectureId(lectureId);
                attendance.setStudentId(student.getStudentId());
                attendance.setCourseId(courseId);
                attendance.setStudentName(student.getName());
                attendance.setSessions(new ArrayList<>());
                attendance.setFirstCheckTimes(new ArrayList<>());
                attendance.setFirstDetectedAt("undetected");
                attendance.setStatus(null);
                attendanceRepository.save(attendance);
            }
        }

        logger.info("Initialized empty attendance table for lectureId: {}", lectureId);
        return ResponseEntity.ok("Empty attendance table initialized for lecture: " + lectureId);
    }

    @DeleteMapping("/statuses/{lectureId}")
    public ResponseEntity<String> deleteAttendanceStatuses(@PathVariable String lectureId) {
        List<AttendanceEntity> attendances = attendanceRepository.findByLectureId(lectureId);
        if (attendances.isEmpty()) {
            logger.info("No attendance records found for lectureId: {}, nothing to clear", lectureId);
            return ResponseEntity.ok("No attendance records found for lecture: " + lectureId + ", nothing to clear");
        }

        boolean anyStatusChanged = false;
        for (AttendanceEntity attendance : attendances) {
            String oldStatus = attendance.getStatus();
            if (oldStatus != null) {
                anyStatusChanged = true;
                attendance.setStatus(null);
                attendanceRepository.save(attendance);

                if ("Absent".equalsIgnoreCase(oldStatus)) {
                    String studentId = attendance.getStudentId();
                    StudentEntity student = studentRepository.findById(studentId).orElse(null);
                    if (student != null) {
                        String courseId = attendance.getCourseId();
                        int currentAbsences = student.getCourseAbsences().getOrDefault(courseId, 0);
                        int newAbsences = Math.max(0, currentAbsences - 1);
                        student.getCourseAbsences().put(courseId, newAbsences);
                        studentRepository.save(student);
                    }
                }
            }
        }

        logger.info("Cleared attendance statuses for lectureId: {}, changed: {}", lectureId, anyStatusChanged);
        if (!anyStatusChanged) {
            return ResponseEntity.ok("All statuses were already null for lecture: " + lectureId);
        }

        return ResponseEntity.ok("Attendance statuses cleared for lecture: " + lectureId);
    }

    private String getStudentName(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .map(StudentEntity::getName)
                .orElse("Unknown");
    }
}