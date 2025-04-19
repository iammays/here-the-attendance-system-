package com.here.backend.Attendance;

import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;
import com.here.backend.Teacher.TeacherEntity;
import com.here.backend.Teacher.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wf-reports")
public class WfStatusController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Map<String, Object>>> getWfReportsForTeacher(@PathVariable String teacherId) {
        List<CourseEntity> courses = courseRepository.findByTeacherId(teacherId);
        List<Map<String, Object>> reports = new ArrayList<>();
        Optional<TeacherEntity> teacher = teacherRepository.findById(teacherId);

        for (CourseEntity course : courses) {
            String courseId = course.getCourseId();
            String courseName = course.getName();

            List<StudentEntity> students = studentRepository.findAll();
            long redundantOccurrences = courseRepository.findAll().stream()
                    .filter(c -> courseName.equals(c.getName()))
                    .count();

            for (StudentEntity student : students) {
                if (student.getCourseId().contains(courseId)) {
                    int absences = student.getCourseAbsences().getOrDefault(courseId, 0);
                    boolean isWf = absences + redundantOccurrences > (redundantOccurrences * 2);

                    if (isWf) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("studentId", student.getStudentId());
                        row.put("studentName", student.getName());
                        row.put("courseId", courseId); // Added courseId to the response
                        row.put("courseName", courseName);
                        row.put("teacherName", teacher.map(TeacherEntity::getName).orElse("Unknown"));
                        row.put("date", LocalDate.now().toString());

                        String wfState = student.getCourseWfStatus().getOrDefault(courseId, "Pending");
                        if ("Approved".equalsIgnoreCase(wfState)) {
                            row.put("status", "Approved");
                        } else if ("Ignored".equalsIgnoreCase(wfState)) {
                            row.put("status", "Ignored");
                        } else {
                            row.put("status", "Pending");
                        }

                        reports.add(row);
                    }
                }
            }
        }

        return ResponseEntity.ok(reports);
    }

}
