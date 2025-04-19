// backend/src/main/java/com/here/backend/Attendance/AttendanceController.java
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
    // حفظ سجل حضور من الـ AI  و ببعت ايميل للطالب لو غايب


@PostMapping
public ResponseEntity<String> saveAttendance(@RequestBody AttendanceRecord record) {
    AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(record.getLectureId(), record.getStudentId());

    if (attendance == null) {
        attendance = new AttendanceEntity();
        attendance.setAttendanceId(UUID.randomUUID().toString());
        attendance.setLectureId(record.getLectureId());
        attendance.setStudentId(record.getStudentId());
        attendance.setCourseId(record.getLectureId().split("-")[0]);
        attendance.setStudentName(getStudentName(record.getStudentId()));
        attendance.setSessions(new ArrayList<>());
    }

    attendance.getSessions().add(new AttendanceEntity.SessionAttendance(record.getSessionId(), record.getDetectionTime()));
    attendance.setStatus(record.getStatus());
    attendanceRepository.save(attendance);

    // Email + WF logic if student is absent
    if ("absent".equalsIgnoreCase(attendance.getStatus())) {
        String studentId = attendance.getStudentId();
        String courseId = attendance.getLectureId();

        StudentEntity student = studentRepository.findById(studentId).orElse(null);
        if (student != null && student.getCourseId().contains(courseId)) {
            int currentAbsences = student.getCourseAbsences().getOrDefault(courseId, 0);

            long redundantOccurrences = 0;
            String courseName = "Unknown";
            try {
                String courseNameJson = courseRepository.findNameByCourseId(courseId);
                JsonNode jsonNode = new ObjectMapper().readTree(courseNameJson);
                courseName = jsonNode.get("name").asText();

                final String finalCourseName = courseName; // must be final for lambda
                redundantOccurrences = courseRepository.findAll().stream()
                        .filter(course -> finalCourseName.equals(course.getName()))
                        .count();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            int newAbsences = currentAbsences + 1;
            student.getCourseAbsences().put(courseId, newAbsences);
            studentRepository.save(student);

            String teacherId = courseRepository.findByCourseId(courseId)
                    .map(CourseEntity::getTeacherId)
                    .orElse(null);
            String teacherEmail = (teacherId != null) ?
                    teacherRepository.findById(teacherId).map(TeacherEntity::getEmail).orElse(null) : null;

            String advisorId = student.getAdvisor();
            String advisorEmail = teacherRepository.findById(advisorId).map(TeacherEntity::getEmail).orElse(null);

            String emailSubject = "Absence Alert: " + courseId;
            String emailBody = "An absence has been recorded for you in the course " + courseId + ".";

            if (newAbsences < redundantOccurrences) {
                emailSubject = "Absence Warning: " + courseId;
                emailBody = "Warning: You have received your Absence Warning due to absences in course " + courseId + ".";
            }

            if (newAbsences == redundantOccurrences + 1) {
                emailSubject = "First Detention Warning: " + courseId;
                emailBody = "Warning: You have received your first detention due to multiple absences in course " + courseId + ".";
            }

            if (newAbsences + redundantOccurrences > (redundantOccurrences * 2)) {
                wfStatusService.checkWfStatus(studentId, courseId);
            }

            emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
        }
    }

    return ResponseEntity.ok("Attendance saved");
}

// دالة مساعدة لجلب اسم الطالب من StudentRepository
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
            @RequestParam(required = false) String search, // البحث باسم الطالب أو رقمه
            @RequestParam(required = false, defaultValue = "studentId") String sortBy, // الترتيب حسب studentId أو status
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        List<AttendanceEntity> attendances = attendanceRepository.findByLectureId(lectureId);
        List<Map<String, Object>> table = new ArrayList<>();

        for (AttendanceEntity attendance : attendances) {
            Map<String, Object> row = new HashMap<>();
            row.put("studentId", attendance.getStudentId());
            row.put("status", attendance.getStatus());
            row.put("sessions", attendance.getSessions().stream()
                    .map(s -> "Session " + s.getSessionId() + ": " + s.getFirstDetectionTime())
                    .collect(Collectors.toList()));
            table.add(row);
        }

        // البحث باسم الطالب أو رقمه
        if (search != null && !search.isEmpty()) {
            table = table.stream()
                    .filter(row -> ((String) row.get("studentId")).toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // الترتيب حسب studentId (أبجدياً) أو status
        Comparator<Map<String, Object>> comparator;
        if ("status".equals(sortBy)) {
            comparator = Comparator.comparing(row -> (String) row.get("status"));
        } else {
            comparator = Comparator.comparing(row -> (String) row.get("studentId")); // ترتيب أبجدي افتراضي
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
}