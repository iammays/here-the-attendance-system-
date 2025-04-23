package com.here.backend.Attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Emails.EmailSenderService;
import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;
import com.here.backend.Teacher.TeacherEntity;
import com.here.backend.Teacher.TeacherRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StudentRepository studentRepository; // إضافة الـ Dependency

    @Autowired
    private CourseRepository courseRepository; // To get the course name
    
    @Autowired
    private EmailSenderService emailSenderService; // To send emails

    @Autowired
    private TeacherRepository teacherRepository; // To send emails

    @Autowired
    private WfStatusService wfStatusService;
    // حفظ سجل حضور من الـ AI و ببعت ايميل للطالب لو غايب


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

            if (courseRepository.findByLectureId(lectureId).isPresent()) {
                int counter = 1;
                String newLectureId = lectureId;
                while (courseRepository.findByLectureId(newLectureId).isPresent()) {
                    newLectureId = lectureId + "_" + counter;
                    counter++;
                }
                lectureId = newLectureId;
            }
        }

        CourseEntity course = courseRepository.findByCourseId(courseId)
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

                String courseName = courseRepository.findByCourseId(courseId)
                        .map(CourseEntity::getName)
                        .orElse("Course Not Found");

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
                    System.err.println("Failed to send absence email for student " + studentId + ": " + e.getMessage());
                }

                try {
                    wfStatusService.checkWfStatus(studentId, courseId);
                } catch (Exception e) {
                    System.err.println("Error checking WF status for student " + studentId + ": " + e.getMessage());
                }
            }
        }

        return ResponseEntity.ok("Attendance saved with lectureId: " + lectureId);
    }

    private String getStudentName(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .map(StudentEntity::getName)
                .orElse("Unknown");
    }


    // تعديل حالة الحضور يدوياً من قبل المعلم
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

    // كرود جديد لتحديث حالة الحضور مع إرسال إيميل في حالة الغياب وتحديث عدد الغيابات
    @PutMapping("/updateWithEmail/{lectureId}/{studentId}")
    public ResponseEntity<String> updateAttendanceStatusWithEmail(
            @PathVariable String lectureId,
            @PathVariable String studentId,
            @RequestBody Map<String, String> body) {
        // تعليق: هذا الكرود يسمح بتحديث حالة الحضور (Present, Late, Absent, Excuse) مع إرسال إيميل إذا كانت الحالة الجديدة Absent
        // كما يقوم بتحديث عدد الغيابات في StudentEntity إذا تغيرت الحالة إلى Absent أو من Absent إلى حالة أخرى
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

            StudentEntity student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                System.err.println("Student not found: " + studentId);
                return ResponseEntity.ok("Attendance status updated to " + newStatus + ", but student not found for absence tracking");
            }

            String courseId = attendance.getCourseId();
            String courseName = courseRepository.findByCourseId(courseId)
                    .map(CourseEntity::getName)
                    .orElseGet(() -> {
                        System.err.println("Course not found for courseId: " + courseId);
                        return "Course Not Found";
                    });

            // تحديث عدد الغيابات
            Map<String, Integer> courseAbsences = student.getCourseAbsences();
            int currentAbsences = courseAbsences.getOrDefault(courseId, 0);

            if ("Absent".equalsIgnoreCase(newStatus) && !"Absent".equalsIgnoreCase(oldStatus)) {
                // الحالة الجديدة Absent والقديمة ليست Absent
                int newAbsences = currentAbsences + 1;
                courseAbsences.put(courseId, newAbsences);
                studentRepository.save(student);

                // إرسال إيميل
                String teacherId = courseRepository.findByCourseId(courseId)
                        .map(CourseEntity::getTeacherId)
                        .orElse(null);
                String teacherEmail = (teacherId != null) ?
                        teacherRepository.findById(teacherId).map(TeacherEntity::getEmail).orElse(null) : null;

                String advisorId = student.getAdvisor();
                String advisorEmail = teacherRepository.findById(advisorId).map(TeacherEntity::getEmail).orElse(null);

                String emailSubject = "Absence Alert: " + courseName;
                String emailBody = "Dear " + student.getName() + ",\nAn absence has been recorded for you in the course " + courseName + ".";

                try {
                    if (student.getEmail() != null) {
                        emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
                        System.out.println("Absence email sent to student: " + student.getEmail());
                    } else {
                        System.err.println("No email found for student: " + studentId);
                    }
                    if (teacherEmail != null) {
                        emailSenderService.sendSimpleEmail(teacherEmail, emailSubject, 
                                "Absence recorded for " + student.getName() + " in course " + courseName);
                        System.out.println("Absence email sent to teacher: " + teacherEmail);
                    }
                    if (advisorEmail != null) {
                        emailSenderService.sendSimpleEmail(advisorEmail, emailSubject, emailBody);
                        System.out.println("Absence email sent to advisor: " + advisorEmail);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to send absence email for student " + studentId + ": " + e.getMessage());
                }

                // التحقق من حالة WF
                try {
                    wfStatusService.checkWfStatus(studentId, courseId);
                } catch (Exception e) {
                    System.err.println("Error checking WF status for student " + studentId + ": " + e.getMessage());
                }
            } else if (!"Absent".equalsIgnoreCase(newStatus) && "Absent".equalsIgnoreCase(oldStatus)) {
                // الحالة الجديدة ليست Absent والقديمة كانت Absent
                int newAbsences = Math.max(0, currentAbsences - 1); // لا يمكن أن يكون أقل من 0
                courseAbsences.put(courseId, newAbsences);
                studentRepository.save(student);
            }

            return ResponseEntity.ok("Attendance status updated to " + newStatus);
        } catch (Exception e) {
            System.err.println("Error updating attendance status for lectureId: " + lectureId + ", studentId: " + studentId + ": " + e.getMessage());
            return ResponseEntity.status(500).body("Error updating attendance status: " + e.getMessage());
        }
    }

    // حذف جدول الحضور لمحاضرة معينة
    @DeleteMapping("/{lectureId}")
    public ResponseEntity<String> deleteAttendance(@PathVariable String lectureId) {
        attendanceRepository.deleteByLectureId(lectureId);
        return ResponseEntity.ok("Attendance records for lecture " + lectureId + " deleted");
    }

    // جلب جدول الحضور مع البحث باسم الطالب أو رقمه والترتيب
    @GetMapping("/table/{lectureId}")
public ResponseEntity<List<Map<String, Object>>> getAttendanceTable(
        @PathVariable String lectureId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false, defaultValue = "studentId") String sortBy,
        @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
    List<AttendanceEntity> attendances = attendanceRepository.findByLectureId(lectureId);
    List<Map<String, Object>> table = new ArrayList<>();

    for (AttendanceEntity attendance : attendances) {
        Map<String, Object> row = new HashMap<>();
        String studentId = attendance.getStudentId();
        
        row.put("studentId", studentId);
        row.put("courseId", attendance.getCourseId());
        row.put("status", attendance.getStatus());

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

        String firstDetectedAt = sessions.stream()
                .filter(s -> !s.get("firstDetectionTime").equals("undetected"))
                .findFirst()
                .map(s -> s.get("firstDetectionTime").toString())
                .orElse("undetected");
        row.put("firstDetectedAt", firstDetectedAt);

        // إضافة firstCheckTimes إلى الاستجابة
        List<Map<String, Object>> firstCheckTimes = attendance.getFirstCheckTimes() != null ?
                attendance.getFirstCheckTimes().stream()
                        .map(c -> {
                            Map<String, Object> checkTimeInfo = new HashMap<>();
                            checkTimeInfo.put("sessionId", c.getSessionId());
                            checkTimeInfo.put("firstCheckTime", c.getFirstCheckTime() != null ? 
                                    c.getFirstCheckTime() : "undetected");
                            return checkTimeInfo;
                        })
                        .collect(Collectors.toList()) :
                new ArrayList<>();
        row.put("firstCheckTimes", firstCheckTimes);

        table.add(row);
    }

    // البحث والترتيب
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
            comparator = Comparator.comparing(row -> (String) row.get("status"));
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
    
            // Set WF status to "Approved"
            wfStatus.put(courseId, "Approved");
            studentRepository.save(student);
    
            // Fetch emails
            String teacherId = courseRepository.findByCourseId(courseId)
                    .map(CourseEntity::getTeacherId)
                    .orElse(null);
            String teacherEmail = (teacherId != null) ?
                    teacherRepository.findById(teacherId)
                            .map(TeacherEntity::getEmail)
                            .orElse(null) : null;
            String advisorId = student.getAdvisor();
            String advisorEmail = teacherRepository.findById(advisorId)
                    .map(TeacherEntity::getEmail)
                    .orElse(null);
    
            // Send email notifications
            String emailSubject = "WF Approved: " + courseId;
            String emailBody = "Your WF status for course " + courseId + " has been approved.";
    
            if (student.getEmail() != null) {
                emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
                System.out.println("WF status email sent to student: " + student.getEmail());
            }
            if (teacherEmail != null) {
                emailSenderService.sendSimpleEmail(teacherEmail, emailSubject,
                        "You approved WF for " + student.getName() + " in course " + courseId);
                System.out.println("WF status email sent to teacher: " + teacherEmail);
            }
            if (advisorEmail != null) {
                emailSenderService.sendSimpleEmail(advisorEmail, emailSubject, emailBody);
                System.out.println("WF status email sent to advisor: " + advisorEmail);
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

        // Set WF status to "Ignored"
        wfStatus.put(courseId, "Ignored");
        studentRepository.save(student);

        // Fetch emails
        String teacherId = courseRepository.findByCourseId(courseId)
                .map(CourseEntity::getTeacherId)
                .orElse(null);
        String teacherEmail = (teacherId != null) ?
                teacherRepository.findById(teacherId)
                        .map(TeacherEntity::getEmail)
                        .orElse(null) : null;
        String advisorId = student.getAdvisor();
        String advisorEmail = teacherRepository.findById(advisorId)
                .map(TeacherEntity::getEmail)
                .orElse(null);

        // Send email notifications
        String emailSubject = "WF Ignored: " + courseId;
        String emailBody = "Your WF request for course " + courseId + " has been ignored.";

        if (student.getEmail() != null) {
            emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
            System.out.println("WF ignore email sent to student: " + student.getEmail());
        }
        if (teacherEmail != null) {
            emailSenderService.sendSimpleEmail(teacherEmail, emailSubject,
                    "You ignored WF for " + student.getName() + " in course " + courseId);
            System.out.println("WF ignore email sent to teacher: " + teacherEmail);
        }
        if (advisorEmail != null) {
            emailSenderService.sendSimpleEmail(advisorEmail, emailSubject, emailBody);
            System.out.println("WF ignore email sent to advisor: " + advisorEmail);
        }

        return ResponseEntity.ok("WF status set to Ignored for course " + courseId);
    }).orElse(ResponseEntity.badRequest().body("Student not found."));
}


@PostMapping("/check-wf-status")
public ResponseEntity<String> checkWfStatus(@RequestParam String studentId, @RequestParam String courseId) {
    return studentRepository.findById(studentId).map(student -> {
        Map<String, String> wfStatus = student.getCourseWfStatus();

        // Fetch teacher and advisor emails
        String teacherId = courseRepository.findByCourseId(courseId)
                .map(CourseEntity::getTeacherId)
                .orElse(null);
        String teacherEmail = (teacherId != null) ?
                teacherRepository.findById(teacherId)
                        .map(TeacherEntity::getEmail)
                        .orElse(null) : null;
        String advisorId = student.getAdvisor();
        String advisorEmail = teacherRepository.findById(advisorId)
                .map(TeacherEntity::getEmail)
                .orElse(null);

        // Check WF status
        String status = wfStatus.getOrDefault(courseId, "Pending");

        if ("Approved".equalsIgnoreCase(status)) {
            // WF is approved, send emails
            String emailSubject = "WF Approved: " + courseId;
            String emailBody = "Your WF status for course " + courseId + " has been approved due to excessive absences.";

            if (student.getEmail() != null) {
                emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
                System.out.println("WF email sent to student: " + student.getEmail());
            }
            if (teacherEmail != null) {
                emailSenderService.sendSimpleEmail(teacherEmail, emailSubject,
                        "You approved WF for " + student.getName() + " in course " + courseId);
                System.out.println("WF email sent to teacher: " + teacherEmail);
            }
            if (advisorEmail != null) {
                emailSenderService.sendSimpleEmail(advisorEmail, emailSubject, emailBody);
                System.out.println("WF email sent to advisor: " + advisorEmail);
            }
            return ResponseEntity.ok("WF is approved for course " + courseId);
        } else {
            return ResponseEntity.ok("WF is not approved for course " + courseId);
        }
    }).orElse(ResponseEntity.badRequest().body("Student not found."));
}


//     @PostMapping("/finalize/{lectureId}")
// public ResponseEntity<String> finalizeAttendance(@PathVariable String lectureId) {
//     List<AttendanceEntity> attendances = attendanceRepository.findByLectureId(lectureId);
//     if (attendances == null || attendances.isEmpty()) {
//         return ResponseEntity.badRequest().body("No attendance records found for lecture " + lectureId);
//     }

//     for (AttendanceEntity attendance : attendances) {
//         try {
//             String finalStatus = attendanceService.determineStatus(lectureId, attendance.getStudentId(), 5);
//             // إذا الحالة تغيرت من غير "Absent" إلى "Absent"، أرسل إيميل
//             if (!"Absent".equals(attendance.getStatus()) && "Absent".equals(finalStatus)) {
//                 attendanceService.sendAbsenceEmail(lectureId, attendance.getStudentId());
//             }
//             attendance.setStatus(finalStatus);
//             attendanceRepository.save(attendance);
//         } catch (Exception e) {
//             System.out.println("Error processing attendance for student " + attendance.getStudentId() + ": " + e.getMessage());
//             e.printStackTrace();
//             return ResponseEntity.status(500).body("Error finalizing attendance: " + e.getMessage());
//         }
//     }
//     return ResponseEntity.ok("Attendance finalized and emails sent for lecture " + lectureId);
// }

    // الكرودات الجديدة المضافة لتلبية الطلب

    // كرود لإضافة يوم جديد مؤقت في أسبوع معين مع محاضرة مؤقتة
    @PostMapping("/addNewDay")
    public ResponseEntity<String> addNewDay(@RequestBody Map<String, String> request) {
        // تعليق: هذا الكرود بيسمح بإضافة يوم جديد في أسبوع معين (مثل الأسبوع الخامس) مع محاضرة مؤقتة.
        // البيانات المطلوبة: معرف الكورس، التاريخ (YYYY-MM-DD)، وقت البداية (HH:mm)، وقت النهاية (HH:mm)، اسم اليوم (مثل Monday)، رقم الغرفة (اختياري).
        // بيتأكد إنه ما في تعارض في الوقت مع محاضرات أخرى لنفس الكورس في نفس اليوم.
        // إذا كان في محاضرة في نفس اليوم، بيضيف المحاضرة الجديدة تحت نفس اليوم طالما ما في تعارض في الوقت.
        // المحاضرة بتكون مؤقتة (ما بتتكرر كل أسبوع) وبيتم إنشاء lectureId بنمط: courseId-date-startTime.

        String courseId = request.get("courseId");
        String date = request.get("date"); // التاريخ بصيغة YYYY-MM-DD
        String startTime = request.get("startTime"); // بصيغة HH:mm
        String endTime = request.get("endTime"); // بصيغة HH:mm
        String day = request.get("day"); // اسم اليوم (مثل Monday)
        String roomId = request.getOrDefault("roomId", ""); // رقم الغرفة (اختياري)

        // التحقق من وجود البيانات المطلوبة
        if (courseId == null || date == null || startTime == null || endTime == null || day == null) {
            return ResponseEntity.badRequest().body("يجب إدخال معرف الكورس، التاريخ، وقت البداية، وقت النهاية، واسم اليوم");
        }

        try {
            // تحويل التاريخ ووقت البداية والنهاية للتحقق من الصلاحية
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("صيغة التاريخ أو الوقت غير صحيحة");
        }

        // التحقق من وجود الكورس
        Optional<CourseEntity> courseOpt = courseRepository.findByCourseId(courseId);
        if (!courseOpt.isPresent()) {
            return ResponseEntity.badRequest().body("الكورس غير موجود");
        }
        CourseEntity course = courseOpt.get();

        // إنشاء lectureId بنمط: courseId-date-startTime
        String lectureId = courseId + "-" + date + "-" + startTime.replace(":", "");

        // التحقق من عدم وجود محاضرة بنفس lectureId
        if (courseRepository.findByLectureId(lectureId).isPresent()) {
            return ResponseEntity.badRequest().body("المحاضرة موجودة مسبقًا بنفس التاريخ والوقت");
        }

        // التحقق من عدم وجود تعارض في الوقت مع محاضرات أخرى لنفس الكورس في نفس اليوم
        List<CourseEntity> existingLectures = courseRepository.findByDay(day).stream()
                .filter(c -> c.getCourseId().equals(courseId) && c.getLectureId() != null)
                .collect(Collectors.toList());

        LocalTime newStart = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime newEnd = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));

        for (CourseEntity existing : existingLectures) {
            try {
                LocalTime existingStart = LocalTime.parse(existing.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime existingEnd = LocalTime.parse(existing.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));

                // التحقق من التعارض: إذا كان وقت البداية أو النهاية يتداخل مع محاضرة أخرى
                if (!(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd))) {
                    return ResponseEntity.badRequest().body("يوجد تعارض في الوقت مع محاضرة أخرى لنفس الكورس في نفس اليوم");
                }
            } catch (DateTimeParseException e) {
                continue; // تجاهل المحاضرات ذات الوقت غير الصالح
            }
        }

        // إنشاء كائن المحاضرة المؤقتة
        CourseEntity newLecture = new CourseEntity();
        newLecture.setCourseId(courseId);
        newLecture.setLectureId(lectureId);
        newLecture.setName(course.getName());
        newLecture.setRoomId(roomId);
        newLecture.setTeacherId(course.getTeacherId());
        newLecture.setStartTime(startTime);
        newLecture.setEndTime(endTime);
        newLecture.setDay(day);
        newLecture.setCategory(course.getCategory());
        newLecture.setCredits(course.getCredits());

        // حفظ المحاضرة في قاعدة البيانات
        courseRepository.save(newLecture);
        return ResponseEntity.ok("تم إضافة يوم جديد مع المحاضرة المؤقتة بنجاح، معرف المحاضرة: " + lectureId);
    }

    // كرود لإضافة محاضرة جديدة مؤقتة في يوم موجود
    @PostMapping("/addNewLecture")
    public ResponseEntity<String> addNewLecture(@RequestBody Map<String, String> request) {
        // تعليق: هذا الكرود بيسمح بإضافة محاضرة إضافية مؤقتة في يوم موجود (مثل الإثنين في الأسبوع الخامس).
        // البيانات المطلوبة: معرف الكورس، التاريخ (YYYY-MM-DD)، وقت البداية (HH:mm)، وقت النهاية (HH:mm)، اسم اليوم (مثل Monday)، رقم الغرفة (اختياري).
        // بيتأكد إنه ما في تعارض في الوقت مع محاضرات أخرى لنفس الكورس في نفس اليوم.
        // المحاضرة بتكون مؤقتة (ما بتتكرر كل أسبوع) وبيتم إنشاء lectureId بنمط: courseId-date-startTime.

        String courseId = request.get("courseId");
        String date = request.get("date");
        String startTime = request.get("startTime");
        String endTime = request.get("endTime");
        String day = request.get("day");
        String roomId = request.getOrDefault("roomId", "");

        if (courseId == null || date == null || startTime == null || endTime == null || day == null) {
            return ResponseEntity.badRequest().body("يجب إدخال معرف الكورس، التاريخ، وقت البداية، وقت النهاية، واسم اليوم");
        }

        try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("صيغة التاريخ أو الوقت غير صحيحة");
        }

        Optional<CourseEntity> courseOpt = courseRepository.findByCourseId(courseId);
        if (!courseOpt.isPresent()) {
            return ResponseEntity.badRequest().body("الكورس غير موجود");
        }
        CourseEntity course = courseOpt.get();

        String lectureId = courseId + "-" + date + "-" + startTime.replace(":", "");

        if (courseRepository.findByLectureId(lectureId).isPresent()) {
            return ResponseEntity.badRequest().body("المحاضرة موجودة مسبقًا بنفس التاريخ والوقت");
        }

        List<CourseEntity> existingLectures = courseRepository.findByDay(day).stream()
                .filter(c -> c.getCourseId().equals(courseId) && c.getLectureId() != null)
                .collect(Collectors.toList());

        LocalTime newStart = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime newEnd = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));

        for (CourseEntity existing : existingLectures) {
            try {
                LocalTime existingStart = LocalTime.parse(existing.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime existingEnd = LocalTime.parse(existing.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));

                if (!(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd))) {
                    return ResponseEntity.badRequest().body("يوجد تعارض في الوقت مع محاضرة أخرى لنفس الكورس في نفس اليوم");
                }
            } catch (DateTimeParseException e) {
                continue;
            }
        }

        CourseEntity newLecture = new CourseEntity();
        newLecture.setCourseId(courseId);
        newLecture.setLectureId(lectureId);
        newLecture.setName(course.getName());
        newLecture.setRoomId(roomId);
        newLecture.setTeacherId(course.getTeacherId());
        newLecture.setStartTime(startTime);
        newLecture.setEndTime(endTime);
        newLecture.setDay(day);
        newLecture.setCategory(course.getCategory());
        newLecture.setCredits(course.getCredits());

        courseRepository.save(newLecture);
        return ResponseEntity.ok("تم إضافة المحاضرة المؤقتة بنجاح، معرف المحاضرة: " + lectureId);
    }

    // كرود لحذف محاضرة مؤقتة مع الاحتفاظ بسجل الحضور
    @DeleteMapping("/deleteTempLecture/{lectureId}")
    public ResponseEntity<String> deleteTempLecture(@PathVariable String lectureId) {
    Optional<CourseEntity> lectureOpt = courseRepository.findByLectureId(lectureId);
    if (!lectureOpt.isPresent()) {
        return ResponseEntity.badRequest().body("المحاضرة غير موجودة");
      }
    courseRepository.delete(lectureOpt.get());
    return ResponseEntity.ok("تم حذف المحاضرة المؤقتة بنجاح، مع الاحتفاظ بسجل الحضور");
    }

    // كرود لتتبع أول وقت اكتشاف للوجه وعدد مرات الاكتشاف
    @GetMapping("/trackFirstDetection/{lectureId}/{studentId}")
    public ResponseEntity<Map<String, Object>> trackFirstDetection(
            @PathVariable String lectureId,
            @PathVariable String studentId) {
        try {
            AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(lectureId, studentId);
            if (attendance == null || attendance.getSessions() == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "لا يوجد سجل حضور لهذا الطالب في هذه المحاضرة");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            int totalSessions = attendance.getSessions().size();
            long detectionCount = attendance.getSessions().stream()
                    .filter(s -> s.getFirstDetectionTime() != null && !s.getFirstDetectionTime().equals("undetected"))
                    .count();

            List<Map<String, Object>> sessions = attendance.getSessions().stream()
                    .map(s -> {
                        Map<String, Object> sessionInfo = new HashMap<>();
                        sessionInfo.put("sessionId", s.getSessionId());
                        sessionInfo.put("firstDetectionTime", s.getFirstDetectionTime() != null ? s.getFirstDetectionTime() : "undetected");
                        return sessionInfo;
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> firstCheckTimes = attendance.getFirstCheckTimes() != null ?
                    attendance.getFirstCheckTimes().stream()
                            .map(c -> {
                                Map<String, Object> checkTimeInfo = new HashMap<>();
                                checkTimeInfo.put("sessionId", c.getSessionId());
                                checkTimeInfo.put("firstCheckTime", c.getFirstCheckTime() != null ? c.getFirstCheckTime() : "undetected");
                                return checkTimeInfo;
                            })
                            .collect(Collectors.toList()) :
                    new ArrayList<>();

            String firstDetectedAt = sessions.stream()
                    .filter(s -> !s.get("firstDetectionTime").equals("undetected"))
                    .findFirst()
                    .map(s -> s.get("firstDetectionTime").toString())
                    .orElse("undetected");

            Map<String, Object> response = new HashMap<>();
            response.put("lectureId", lectureId);
            response.put("studentId", studentId);
            response.put("courseId", attendance.getCourseId());
            response.put("totalSessions", totalSessions);
            response.put("detectionCount", detectionCount);
            response.put("sessions", sessions);
            response.put("firstCheckTimes", firstCheckTimes);
            response.put("firstDetectedAt", firstDetectedAt);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in trackFirstDetection for lectureId: " + lectureId + 
                    ", studentId: " + studentId + ": " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "خطأ داخلي في معالجة البيانات: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
 
}