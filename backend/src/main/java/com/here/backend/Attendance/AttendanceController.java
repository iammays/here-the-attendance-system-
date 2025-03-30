package com.here.backend.Attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Emails.EmailSenderService;
import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;
import com.here.backend.Teacher.TeacherEntity;
import com.here.backend.Teacher.TeacherRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;


    @Autowired
    private StudentRepository studentRepository; // To access student data (email)
    
    @Autowired
    private CourseRepository courseRepository; // To get the course name
    
    @Autowired
    private EmailSenderService emailSenderService; // To send emails

    @Autowired
    private TeacherRepository teacherRepository; // To send emails

    @Autowired
    private WfStatusService wfStatusService;

    // // Create a new attendance record
    // @PostMapping
    // public AttendanceEntity createAttendance(@RequestBody AttendanceEntity attendanceEntity) {
    //     return attendanceRepository.save(attendanceEntity);
    // }

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




//     @PostMapping
//     public ResponseEntity<AttendanceEntity> createAttendance(@RequestBody AttendanceEntity attendanceEntity) {
//         // Save the attendance record
//         AttendanceEntity savedAttendance = attendanceRepository.save(attendanceEntity);
    
//         // Process only if the student is marked absent
//         if ("absent".equalsIgnoreCase(attendanceEntity.getStatus())) {
//             String studentId = attendanceEntity.getStudentId();
//             String courseId = attendanceEntity.getSessionId();
    
//             // Find student by ID
//             studentRepository.findById(studentId).ifPresent(student -> {
//                 // Check if the student is enrolled in the course
//                 if (student.getCourseId().contains(courseId)) {
    
//                     // Get the current absence count for the course
//                     int currentAbsences = student.getCourseAbsences().getOrDefault(courseId, 0);
//                     System.out.println("Current Absences for course " + courseId + ": " + currentAbsences);
    


//                     String courseNamee = courseRepository.findNameByCourseId(courseId);
//                    ObjectMapper objectMapper = new ObjectMapper();
// JsonNode jsonNode = null;
// try {
//     jsonNode = objectMapper.readTree(courseNamee);
// } catch (JsonProcessingException e) {
//     // TODO Auto-generated catch block
//     e.printStackTrace();
// }
// String courseName = jsonNode.get("name").asText();

// System.out.println(courseName); // Output: Course_7

                   
//                     System.out.println("couuursseee naaammeee "+courseName);

//                     long redundantOccurrences = courseRepository.findAll().stream()
//                     .peek(course -> System.out.println("Course: " + course)) // Print each course before filtering
//                     .filter(course -> {
//                         System.out.println("Checking course name: " + course.getName());
//                         System.out.println("Comparing '" + courseName + "' with '" + course.getName() + "'");
//                         boolean matches = courseName.equals(course.getName());
//                         System.out.println("Match result: " + matches);
//                         return matches;
//                     })
//                     .count();
                
//                 System.out.println("Final count: " + redundantOccurrences);



//                     System.out.println("Redundant Occurrences for " + courseName + ": " + redundantOccurrences);
//                     // Increment absence count
//                     int newAbsences = currentAbsences + 1;
//                     student.getCourseAbsences().put(courseId, newAbsences);
//                     System.out.println("Updated Absences for course " + courseId + ": " + newAbsences);
    
//                     // Save updated student record
//                     studentRepository.save(student);
    
//                     // Fetch teacher ID from Course Repository
//                     String teacherId = courseRepository.findByCourseId(courseId)
//                             .map(CourseEntity::getTeacherId)
//                             .orElse(null);
//                     System.out.println("Teacher ID: " + teacherId);
    
//                     // Fetch teacher email from Teacher Repository
//                     String teacherEmail = (teacherId != null) ?
//                             teacherRepository.findById(teacherId)
//                                     .map(TeacherEntity::getEmail)
//                                     .orElse(null) : null;
//                     System.out.println("Teacher Email: " + teacherEmail);
    
//                     // Get advisor ID and fetch advisor email from Teacher Repository
//                     String advisorId = student.getAdvisor();
//                     System.out.println("Advisor ID: " + advisorId);
    
//                     String advisorEmail = teacherRepository.findById(advisorId)
//                             .map(TeacherEntity::getEmail)
//                             .orElse(null);
//                     System.out.println("Advisor Email: " + advisorEmail);
    
//                     // Prepare email subject and body
//                     String emailSubject = "Absence Alert: " + courseId;
//                     String emailBody = "An absence has been recorded for you in the course " + courseId + ".";
    
//                     // Determine warnings
//                     if (newAbsences < redundantOccurrences ) {
//                         emailSubject = "Absence Warning: " + courseId;
//                         emailBody = "Warning: You have received your Absence Warning due to absences in course " + courseId + ".";
//                     }
    
//                     // First Detention Warning
//                     if (newAbsences == redundantOccurrences+1) {
                      
//                         emailSubject = "First Detention Warning: " + courseId;
//                         emailBody = "Warning: You have received your first detention due to multiple absences in course " + courseId + ".";
//                     }
//                     //before WF Warning
//                     if (newAbsences > ((redundantOccurrences * 2)-1)) {
//                         emailSubject = " before WF Warning: " + courseId;
//                         emailBody = "Warning: your student "+student.getName()+" will take a wf in the next absence in the course " + courseId + ".";
    
//                         // Notify teacher and advisor if their emails are found
//                         if (teacherEmail != null) {
//                             System.out.println("Sending before WF Warning to Teacher: " + teacherEmail);
//                             emailSenderService.sendSimpleEmail(teacherEmail, "WF Teacher Email: " + courseId, emailBody);
//                         }


  
//                     }

    
//                     // WF Warning
//                     if (newAbsences > redundantOccurrences * 2) {
//                         emailSubject = "WF Warning: " + courseId;
//                         emailBody = "Warning: Your absences have reached a critical level, and you are at risk of being withdrawn from course " + courseId + ".";
    
//                         // Notify teacher and advisor if their emails are found
//                         if (teacherEmail != null && student.getCourseWfStatus().get(courseId)) {
//                             System.out.println("Sending WF Warning to Teacher: " + teacherEmail);
//                             emailSenderService.sendSimpleEmail(teacherEmail, "WF Teacher Email: " + courseId, emailBody);
//                         }
//                         if (advisorEmail != null&& student.getCourseWfStatus().get(courseId)) {
//                             System.out.println("Sending WF Warning to Advisor: " + advisorEmail);
//                             emailSenderService.sendSimpleEmail(advisorEmail, "WF Advisor Email: " + courseId, emailBody);
//                         }
//                         else{
//                             System.out.println("Sending WF Warning to Teacher: " + teacherEmail);
//                             emailSenderService.sendSimpleEmail(teacherEmail, "WF Teacher Email: " + courseId, "pleae approve or ignore the wf of the student");
//                         }

//                         // student.getCourseWfStatus().put(courseId, true); // Set WF for this course
//                         System.out.println("Student WF status set to true for course " + courseId);  
//                         studentRepository.save(student);
//                     }
    
//                     // Send email to student
//                     System.out.println("Sending Email to Student: " + student.getEmail());
//                     emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
//                 }
//             });
//         }
    
//         return ResponseEntity.ok(savedAttendance);
//     }




@PostMapping
public ResponseEntity<AttendanceEntity> createAttendance(@RequestBody AttendanceEntity attendanceEntity) {
    // Save the attendance record
    AttendanceEntity savedAttendance = attendanceRepository.save(attendanceEntity);

    // Process only if the student is marked absent
    if ("absent".equalsIgnoreCase(attendanceEntity.getStatus())) {
        String studentId = attendanceEntity.getStudentId();
        String courseId = attendanceEntity.getSessionId();

        // Find student by ID
        studentRepository.findById(studentId).ifPresent(student -> {
            // Check if the student is enrolled in the course
            if (student.getCourseId().contains(courseId)) {
                // Get the current absence count for the course
                int currentAbsences = student.getCourseAbsences().getOrDefault(courseId, 0);
                System.out.println("Current Absences for course " + courseId + ": " + currentAbsences);

                String courseNamee = courseRepository.findNameByCourseId(courseId);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = null;
                try {
                    jsonNode = objectMapper.readTree(courseNamee);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                String courseName = jsonNode.get("name").asText();
                System.out.println(courseName);

                System.out.println("couuursseee naaammeee " + courseName);

                long redundantOccurrences = courseRepository.findAll().stream()
                        .peek(course -> System.out.println("Course: " + course))
                        .filter(course -> {
                            System.out.println("Checking course name: " + course.getName());
                            System.out.println("Comparing '" + courseName + "' with '" + course.getName() + "'");
                            boolean matches = courseName.equals(course.getName());
                            System.out.println("Match result: " + matches);
                            return matches;
                        })
                        .count();
                System.out.println("Final count: " + redundantOccurrences);

                System.out.println("Redundant Occurrences for " + courseName + ": " + redundantOccurrences);
                // Increment absence count
                int newAbsences = currentAbsences + 1;
                student.getCourseAbsences().put(courseId, newAbsences);
                System.out.println("Updated Absences for course " + courseId + ": " + newAbsences);

                // Save updated student record
                studentRepository.save(student);

                // Fetch teacher ID from Course Repository
                String teacherId = courseRepository.findByCourseId(courseId)
                        .map(CourseEntity::getTeacherId)
                        .orElse(null);
                System.out.println("Teacher ID: " + teacherId);

                // Fetch teacher email from Teacher Repository
                String teacherEmail = (teacherId != null) ?
                        teacherRepository.findById(teacherId)
                                .map(TeacherEntity::getEmail)
                                .orElse(null) : null;
                System.out.println("Teacher Email: " + teacherEmail);

                // Get advisor ID and fetch advisor email from Teacher Repository
                String advisorId = student.getAdvisor();
                System.out.println("Advisor ID: " + advisorId);

                String advisorEmail = teacherRepository.findById(advisorId)
                        .map(TeacherEntity::getEmail)
                        .orElse(null);
                System.out.println("Advisor Email: " + advisorEmail);

                // Prepare email subject and body
                String emailSubject = "Absence Alert: " + courseId;
                String emailBody = "An absence has been recorded for you in the course " + courseId + ".";

                // Determine warnings
                if (newAbsences < redundantOccurrences) {
                    emailSubject = "Absence Warning: " + courseId;
                    emailBody = "Warning: You have received your Absence Warning due to absences in course " + courseId + ".";
                }

                // First Detention Warning
                if (newAbsences == redundantOccurrences + 1) {
                    emailSubject = "First Detention Warning: " + courseId;
                    emailBody = "Warning: You have received your first detention due to multiple absences in course " + courseId + ".";
                }

                // Before WF Warning and WF Status Check
                if (newAbsences > ((redundantOccurrences * 2) - 1)) {
                    emailSubject = "Before WF Warning: " + courseId;
                    emailBody = "Warning: your student " + student.getName() + " will take a WF in the next absence in the course " + courseId + ".";

                    // Notify teacher if email is found
                    if (teacherEmail != null) {
                        System.out.println("Sending before WF Warning to Teacher: " + teacherEmail);
                        emailSenderService.sendSimpleEmail(teacherEmail, "WF Teacher Email: " + courseId, emailBody);
                    }

                    // Call the new WF status check API
                    wfStatusService.checkWfStatus(studentId, courseId);
                }

                // Send email to student
                System.out.println("Sending Email to Student: " + student.getEmail());
                emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
            }
        });
    }

    return ResponseEntity.ok(savedAttendance);
}
// // Updated approve-wf endpoint to toggle WF status
// @PostMapping("/api/attendances/approve-wf")
// public ResponseEntity<String> approveWf(@RequestParam String studentId, @RequestParam String courseId) {
//     return studentRepository.findById(studentId).map(student -> {
//         Map<String, Boolean> wfStatus = student.getCourseWfStatus();

//         // Toggle WF status: if true -> false, if false/null -> true
//         boolean currentStatus = wfStatus.getOrDefault(courseId, false);
//         boolean newStatus = !currentStatus;
//         wfStatus.put(courseId, newStatus);
//         studentRepository.save(student);

//         // Fetch emails
//         String teacherId = courseRepository.findByCourseId(courseId)
//                 .map(CourseEntity::getTeacherId)
//                 .orElse(null);
//         String teacherEmail = (teacherId != null) ?
//                 teacherRepository.findById(teacherId)
//                         .map(TeacherEntity::getEmail)
//                         .orElse(null) : null;
//         String advisorId = student.getAdvisor();
//         String advisorEmail = teacherRepository.findById(advisorId)
//                 .map(TeacherEntity::getEmail)
//                 .orElse(null);

//         // Send email based on new status
//         String emailSubject = newStatus ? "WF Approved: " + courseId : "WF Revoked: " + courseId;
//         String emailBody = newStatus ?
//                 "Your WF status for course " + courseId + " has been approved." :
//                 "Your WF status for course " + courseId + " has been revoked.";

//         if (student.getEmail() != null) {
//             emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
//             System.out.println("WF status email sent to student: " + student.getEmail());
//         }
//         if (teacherEmail != null) {
//             emailSenderService.sendSimpleEmail(teacherEmail, emailSubject,
//                     newStatus ?
//                             "You approved WF for " + student.getName() + " in course " + courseId :
//                             "You revoked WF for " + student.getName() + " in course " + courseId);
//             System.out.println("WF status email sent to teacher: " + teacherEmail);
//         }
//         if (advisorEmail != null) {
//             emailSenderService.sendSimpleEmail(advisorEmail, emailSubject, emailBody);
//             System.out.println("WF status email sent to advisor: " + advisorEmail);
//         }

//         return ResponseEntity.ok("WF status changed to " + newStatus + " for course " + courseId);
//     }).orElse(ResponseEntity.badRequest().body("Student not found."));
// }



@PostMapping("/api/attendances/approve-wf")
    public ResponseEntity<String> approveWf(@RequestParam String studentId, @RequestParam String courseId) {
        return studentRepository.findById(studentId).map(student -> {
            Map<String, Boolean> wfStatus = student.getCourseWfStatus();

            // Toggle WF status: if true -> false, if false/null -> true
            boolean currentStatus = wfStatus.getOrDefault(courseId, false);
            boolean newStatus = !currentStatus;
            wfStatus.put(courseId, newStatus);
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

            // Send email based on new status
            String emailSubject = newStatus ? "WF Approved: " + courseId : "WF Revoked: " + courseId;
            String emailBody = newStatus ?
                    "Your WF status for course " + courseId + " has been approved." :
                    "Your WF status for course " + courseId + " has been revoked.";

            if (student.getEmail() != null) {
                emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
                System.out.println("WF status email sent to student: " + student.getEmail());
            }
            if (teacherEmail != null) {
                emailSenderService.sendSimpleEmail(teacherEmail, emailSubject,
                        newStatus ?
                                "You approved WF for " + student.getName() + " in course " + courseId :
                                "You revoked WF for " + student.getName() + " in course " + courseId);
                System.out.println("WF status email sent to teacher: " + teacherEmail);
            }
            if (advisorEmail != null) {
                emailSenderService.sendSimpleEmail(advisorEmail, emailSubject, emailBody);
                System.out.println("WF status email sent to advisor: " + advisorEmail);
            }

            return ResponseEntity.ok("WF status changed to " + newStatus + " for course " + courseId);
        }).orElse(ResponseEntity.badRequest().body("Student not found."));
    }

@PostMapping("/check-wf-status")
    public ResponseEntity<String> checkWfStatus(@RequestParam String studentId, @RequestParam String courseId) {
        return studentRepository.findById(studentId).map(student -> {
            // Check WF status map
            Map<String, Boolean> wfStatus = student.getCourseWfStatus();

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
            boolean isWfApproved = wfStatus.getOrDefault(courseId, false);

            if (isWfApproved) {
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
                // WF is not approved (false or not set)
                return ResponseEntity.ok("WF is not approved for course " + courseId);
            }
        }).orElse(ResponseEntity.badRequest().body("Student not found."));
    }


}
