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
import com.here.backend.Teacher.TeacherRepository;import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

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


//    // Create a new attendance record  with the email
//     @PostMapping
//     public AttendanceEntity createAttendance(@RequestBody AttendanceEntity attendanceEntity) {
//         // Save the attendance record
//         AttendanceEntity savedAttendance = attendanceRepository.save(attendanceEntity);

//         // Check if the status is "absent"
//         if ("absent".equalsIgnoreCase(savedAttendance.getStatus())) {
//             // Get the student's email
//             Optional<StudentEntity> student = studentRepository.findById(savedAttendance.getStudentId());
//             if (student.isPresent()) {
//                 String studentEmail = student.get().getEmail();

//                 // Get the course name
//                 Optional<String> courseName = courseRepository.findById(savedAttendance.getSessionId()).map(course -> course.getName());
//                 String course = courseName.orElse("Unknown Course");

//                 // Send the email notification
//                 String subject = "Absence Recorded in " + course;
//                 String body = "Dear " + student.get().getName() + ",\n\n" +
//                               "An absence has been recorded for you in the course: " + course + ".\n\n" +
//                               "Best regards,\nYour University Team";
//                 emailSenderService.sendSimpleEmail(studentEmail, subject, body);
//             }
//         }

//         return savedAttendance;
//     }

    // // Create a new attendance record and update the student absence count
    // @PostMapping
    // public ResponseEntity<AttendanceEntity> createAttendance(@RequestBody AttendanceEntity attendanceEntity) {
    //     // Save attendance record
    //     AttendanceEntity savedAttendance = attendanceRepository.save(attendanceEntity);

    //     // Update student's absence count
    //     if ("absent".equalsIgnoreCase(attendanceEntity.getStatus())) {
    //         String studentId = attendanceEntity.getStudentId();
    //         String courseId = attendanceEntity.getSessionId();

    //         // Find student by ID
    //         studentRepository.findById(studentId).ifPresent(student -> {
    //             // Check if the student is enrolled in the course
    //             if (student.getCourseId().contains(courseId)) {
    //                 // Increment absence count for the course
    //                 student.getCourseAbsences().put(courseId, student.getCourseAbsences().getOrDefault(courseId, 0) + 1);

    //                 // Save the updated student entity
    //                 studentRepository.save(student);

    //                 // Send an email notification to the student about the absence
    //                 String emailSubject = "Absence in " + courseId;
    //                 String emailBody = "An absence has been recorded for you in course " + courseId + ".";
    //                 emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
    //             }
    //         });
    //     }

    //     return ResponseEntity.ok(savedAttendance);
    // }



    // @PostMapping
    // public ResponseEntity<AttendanceEntity> createAttendance(@RequestBody AttendanceEntity attendanceEntity) {
    //     // Save the attendance record
    //     AttendanceEntity savedAttendance = attendanceRepository.save(attendanceEntity);
    
    //     // Process only if the student is marked absent
    //     if ("absent".equalsIgnoreCase(attendanceEntity.getStatus())) {
    //         String studentId = attendanceEntity.getStudentId();
    //         String courseId = attendanceEntity.getSessionId();
    
    //         // Find student by ID
    //         studentRepository.findById(studentId).ifPresent(student -> {
    //             // Check if the student is enrolled in the course
    //             if (student.getCourseId().contains(courseId)) {
    
    //                 // Get the current absence count for the course
    //                 int currentAbsences = student.getCourseAbsences().getOrDefault(courseId, 0);
    
    //                 // Fetch redundant occurrences of this course from the course repository
    //                 int redundantOccurrences = courseRepository.countByCourseId(courseId); 
    
    //                 // Increment absence count
    //                 int newAbsences = currentAbsences + 1;
    //                 student.getCourseAbsences().put(courseId, newAbsences);
    
    //                 // Save updated student record
    //                 studentRepository.save(student);
    
    //                 // Prepare email subject and body
    //                 String emailSubject = "Absence Alert: " + courseId;
    //                 String emailBody = "An absence has been recorded for you in the course " + courseId + ".";
    
    //                 // Send warnings based on absence count
    //                 if (newAbsences == redundantOccurrences) {
    //                     emailSubject = "First Detention Warning: " + courseId;
    //                     emailBody = "Warning: You have received your first detention due to multiple absences in course " + courseId + ".";
    //                 } else if (newAbsences >= redundantOccurrences * 2) {
    //                     emailSubject = "WF Warning: " + courseId;
    //                     emailBody = "Warning: Your absences have reached a critical level, and you are at risk of being withdrawn from course " + courseId + ".";
    
    //                     // Notify teacher and advisor
    //                     String teacherEmail = getTeacherEmail(student.getTeacherId());
    //                     String advisorEmail = getAdvisorEmail(student.getAdvisor());
                        
    //                     emailSenderService.sendSimpleEmail(teacherEmail, "WF Warning: " + courseId, emailBody);
    //                     emailSenderService.sendSimpleEmail(advisorEmail, "WF Warning: " + courseId, emailBody);
    //                 }
    
    //                 // Send email to student
    //                 emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
    //             }
    //         });
    //     }
    
    //     return ResponseEntity.ok(savedAttendance);
    // }
    

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
    
                    // Fetch redundant occurrences of this course from the course repository
                    // int redundantOccurrences = courseRepository.countByCourseId(courseId);
                    // System.out.println("Redundant Occurrences for course " + courseId + ": " + redundantOccurrences);
                    // String courseName = courseRepository.findByCourseId(courseId)
                    // .map(CourseEntity::getName)
                    // .orElse(null);
                    

                    String courseNamee = courseRepository.findNameByCourseId(courseId);
                   ObjectMapper objectMapper = new ObjectMapper();
JsonNode jsonNode = null;
try {
    jsonNode = objectMapper.readTree(courseNamee);
} catch (JsonProcessingException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
}
String courseName = jsonNode.get("name").asText();

System.out.println(courseName); // Output: Course_7

                    // Loop through the list of courses to find the one with the matching courseId
                    // CourseEntity matchingCourse = allCourses.stream()
                    //     .filter(course -> courseId.equals(course.getCourseId()))  // Match the courseId
                    //     .findFirst()  // Get the first matching course (if any)
                    //     .orElse(null);  // Return null if no match is found
                    
                    // Now check if a course was found and print its name
                    // if (matchingCourse != null) {
                    //      courseName = matchingCourse.getName();
                    //     System.out.println("Course Name: " + courseName);
                    // } else {
                    //     System.out.println("Course not found with ID: " + courseId);
                    // }                    
                    System.out.println("couuursseee naaammeee "+courseName);
                    
                    long redundantOccurrences = courseRepository.findAll().stream()
                    .filter(course -> courseName.equals(course.getName()))
                    .count();

                    // int redundantOccurrences = courseRepository.countByCourseName(courseName);
                    // System.out.println("Redundant Occurrences for course " + courseId + ": " + redundantOccurrences);
                    // int redundantOccurrences = courseRepository.countByCourseName(courseName);
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
                    if (newAbsences == redundantOccurrences) {
                        emailSubject = "First Detention Warning: " + courseId;
                        emailBody = "Warning: You have received your first detention due to multiple absences in course " + courseId + ".";
                    }
    
                    // WF Warning
                    if (newAbsences >= redundantOccurrences * 2) {
                        emailSubject = "WF Warning: " + courseId;
                        emailBody = "Warning: Your absences have reached a critical level, and you are at risk of being withdrawn from course " + courseId + ".";
    
                        // Notify teacher and advisor if their emails are found
                        if (teacherEmail != null) {
                            System.out.println("Sending WF Warning to Teacher: " + teacherEmail);
                            emailSenderService.sendSimpleEmail(teacherEmail, "WF Teacher Email: " + courseId, emailBody);
                        }
                        if (advisorEmail != null) {
                            System.out.println("Sending WF Warning to Advisor: " + advisorEmail);
                            emailSenderService.sendSimpleEmail(advisorEmail, "WF Advisor Email: " + courseId, emailBody);
                        }
                    }
    
                    // Send email to student
                    System.out.println("Sending Email to Student: " + student.getEmail());
                    emailSenderService.sendSimpleEmail(student.getEmail(), emailSubject, emailBody);
                }
            });
        }
    
        return ResponseEntity.ok(savedAttendance);
    }
    


}
