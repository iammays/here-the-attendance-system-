// backend/src/main/java/com/here/backend/Attendance/AttendanceController.java
package com.here.backend.Attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;

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


    // حفظ سجل حضور من الـ AI  و ببعت ايميل للطالب لو غايب
   
    @PostMapping
public ResponseEntity<String> saveAttendance(@RequestBody AttendanceRecord record) {
    AttendanceEntity attendance = attendanceRepository.findByLectureIdAndStudentId(record.getLectureId(), record.getStudentId());
    if (attendance == null) {
        attendance = new AttendanceEntity();
        attendance.setAttendanceId(UUID.randomUUID().toString());
        attendance.setLectureId(record.getLectureId());
        attendance.setStudentId(record.getStudentId());
        attendance.setCourseId(record.getLectureId().split("-")[0]); // استخراج courseId من lectureId
        attendance.setStudentName(getStudentName(record.getStudentId())); // جلب اسم الطالب
        attendance.setSessions(new ArrayList<>());
    }
    attendance.getSessions().add(new AttendanceEntity.SessionAttendance(record.getSessionId(), record.getDetectionTime()));
    attendance.setStatus(record.getStatus()); // الحالة النهائية بتتسجل هنا مباشرة
    attendanceRepository.save(attendance);

    // إرسال إيميل فوراً إذا كان الطالب غايب
    if ("Absent".equals(record.getStatus())) {
        try {
            attendanceService.sendAbsenceEmail(record.getLectureId(), record.getStudentId());
        } catch (Exception e) {
            System.out.println("Failed to send absence email to student " + record.getStudentId() + ": " + e.getMessage());
            e.printStackTrace();
            // ما نوقفش العملية لو الإيميل فشل
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