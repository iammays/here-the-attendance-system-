package com.here.backend.Attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class LectureScheduler {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AttendanceController attendanceController;

    // يشتغل كل يوم أحد الساعة 00:00
    // @Scheduled(cron = "0 0 0 * * SUN")
    // public void generateWeeklyLecturesAndAttendance() {
    //     // تحديد بداية الأسبوع القادم
    //     LocalDate startOfWeek = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
    //     List<CourseEntity> courses = courseRepository.findAll();

    //     for (CourseEntity course : courses) {
    //         String courseId = course.getCourseId();
    //         String day = course.getDay();
    //         String startTime = course.getStartTime();
    //         String endTime = course.getEndTime();
    //         String roomId = course.getRoomId() != null ? course.getRoomId() : "";
    //         String teacherId = course.getTeacherId();
    //         String name = course.getName();
    //         String category = course.getCategory();
    //         Integer credits = course.getCredits();

    //         if (day == null || startTime == null || endTime == null) {
    //             continue; // تخطي الكورس لو البيانات ناقصة
    //         }

    //         // تحديد تاريخ المحاضرة بناءً على اليوم في الأسبوع
    //         LocalDate lectureDate = startOfWeek.with(java.time.temporal.TemporalAdjusters.nextOrSame(
    //             java.time.DayOfWeek.valueOf(day.toUpperCase())
    //         ));

    //         // توليد lectureId بناءً على courseId وتاريخ المحاضرة
    //         String lectureId = courseId + "-" + lectureDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "-" + startTime.replace(":", "");

    //         // التأكد إن المحاضرة مش موجودة مسبقًا
    //         if (!courseRepository.findByLectureId(lectureId).isPresent()) {
    //             // إنشاء سجل المحاضرة
    //             CourseEntity newLecture = new CourseEntity();
    //             newLecture.setCourseId(courseId);
    //             newLecture.setLectureId(lectureId);
    //             newLecture.setName(name);
    //             newLecture.setRoomId(roomId);
    //             newLecture.setTeacherId(teacherId);
    //             newLecture.setStartTime(startTime);
    //             newLecture.setEndTime(endTime);
    //             newLecture.setDay(day);
    //             newLecture.setCategory(category);
    //             newLecture.setCredits(credits);
    //             courseRepository.save(newLecture);

    //             // إنشاء جدول حضور فارغ
    //             attendanceController.initializeAttendanceTable(lectureId);
    //         }
    //     }
    // }
}