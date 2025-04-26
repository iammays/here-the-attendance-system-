package com.here.backend.Attendance;

import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Emails.EmailSenderService;
import com.here.backend.Student.StudentRepository;
import com.here.backend.Teacher.TeacherEntity;
import com.here.backend.Teacher.TeacherRepository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WfStatusService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    public String checkWfStatus(String studentId, String courseId) {
        return studentRepository.findById(studentId).map(student -> {
            Map<String, String> wfStatus = student.getCourseWfStatus();  // Now stores String values
    
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
    
            // Check WF status (compare string)
            String wfState = wfStatus.getOrDefault(courseId, "Pending");
    
            if ("Approved".equalsIgnoreCase(wfState)) {
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
                return "WF is approved for course " + courseId;
            } else {
                return "WF is not approved for course " + courseId;
            }
        }).orElse("Student not found.");
    }
    
}