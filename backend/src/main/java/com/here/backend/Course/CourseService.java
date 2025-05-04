package com.here.backend.Course;

import com.here.backend.Attendance.AttendanceEntity;
import com.here.backend.Attendance.AttendanceRepository;
import com.here.backend.Lecture.LectureEntity;
import com.here.backend.Lecture.LectureRepository;
import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    // حساب جدول الكاميرا لمحاضرة معينة
    public CameraSchedule calculateCameraSchedule(String courseId, int lateThreshold) {
        CourseEntity course = courseRepository.findByCourseId(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        int duration = calculateDuration(course.getStartTime(), course.getEndTime());
        int remainingTime = duration - 10 - lateThreshold;
        int numSessions = duration <= 75 ? 4 : (duration < 120 ? 6 : 8);
        int interval = remainingTime / numSessions;

        return new CameraSchedule(lateThreshold, interval, numSessions);
    }

    // حساب مدة المحاضرة بالدقائق
    private int calculateDuration(String startTime, String endTime) {
        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");
        int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
        int endMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);
        return endMinutes - startMinutes;
    }

    // مهمة مجدولة لتوليد المحاضرات تلقائيًا كل يوم
    @Scheduled(cron = "0 0 0 * * *") // تُنفَّذ يوميًا عند منتصف الليل
    public void generateWeeklyLectures() {
        logger.info("Starting scheduled task to generate weekly lectures for today: {}", LocalDate.now());

        // الحصول على اليوم الحالي
        LocalDate today = LocalDate.now();
        String todayDay = today.getDayOfWeek().toString().toUpperCase(); // مثال: "MONDAY"

        // جلب جميع الكورسات التي تُعقد اليوم
        List<CourseEntity> courses = courseRepository.findByDay(todayDay);
        logger.debug("Found {} courses scheduled for {}", courses.size(), todayDay);

        for (CourseEntity course : courses) {
            String courseId = course.getCourseId();
            String dateStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE); // YYYY-MM-DD
            String startTime = course.getStartTime();
            String lectureId = courseId + "-" + dateStr + "-" + startTime.replace(":", "");

            // التحقق من عدم وجود محاضرة بنفس lectureId
            if (lectureRepository.findByLectureId(lectureId).isPresent()) {
                logger.warn("Lecture already exists for lectureId: {}", lectureId);
                continue;
            }

            // التحقق من التعارضات الزمنية
            List<LectureEntity> existingLectures = lectureRepository.findAll().stream()
                    .filter(l -> l.getCourseId().equals(courseId) && l.getDay().equalsIgnoreCase(todayDay))
                    .toList();

            LocalTime newStart = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime newEnd = LocalTime.parse(course.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));

            boolean hasConflict = false;
            for (LectureEntity existing : existingLectures) {
                try {
                    LocalTime existingStart = LocalTime.parse(existing.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                    LocalTime existingEnd = LocalTime.parse(existing.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
                    if (!(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd))) {
                        logger.warn("Time conflict with existing lecture: {}", existing.getLectureId());
                        hasConflict = true;
                        break;
                    }
                } catch (DateTimeParseException e) {
                    logger.warn("Skipping lecture with invalid time format: {}", existing.getLectureId());
                }
            }

            if (hasConflict) {
                continue;
            }

            // إنشاء محاضرة جديدة
            LectureEntity newLecture = new LectureEntity();
            newLecture.setCourseId(courseId);
            newLecture.setLectureId(lectureId);
            newLecture.setName(course.getName());
            newLecture.setRoomId(course.getRoomId());
            newLecture.setTeacherId(course.getTeacherId());
            newLecture.setStartTime(startTime);
            newLecture.setEndTime(course.getEndTime());
            newLecture.setDay(todayDay);
            newLecture.setCategory(course.getCategory());
            newLecture.setCredits(course.getCredits());
            newLecture.setLateThreshold(course.getLateThreshold());

            try {
                lectureRepository.save(newLecture);
                logger.info("Lecture created successfully: {}", lectureId);

                // تهيئة جدول الحضور
                initializeAttendanceTable(lectureId);
            } catch (Exception e) {
                logger.error("Error creating lecture {}: {}", lectureId, e.getMessage(), e);
            }
        }

        logger.info("Completed generating lectures for today: {}", today);
    }

    // تهيئة جدول الحضور لمحاضرة
    private void initializeAttendanceTable(String lectureId) {
        LectureEntity lecture = lectureRepository.findByLectureId(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found: " + lectureId));
        String courseId = lecture.getCourseId();

        List<StudentEntity> students = studentRepository.findByCourseId(courseId);
        if (students.isEmpty()) {
            logger.warn("No students enrolled in course: {}", courseId);
            return;
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
                logger.debug("Initialized attendance for student {} in lecture {}", student.getStudentId(), lectureId);
            }
        }
        logger.info("Attendance table initialized for lecture: {}", lectureId);
    }
}

class CameraSchedule {
    private int lateThreshold;
    private int interval;
    private int numSessions;

    public CameraSchedule(int lateThreshold, int interval, int numSessions) {
        this.lateThreshold = lateThreshold;
        this.interval = interval;
        this.numSessions = numSessions;
    }

    public int getLateThreshold() { return lateThreshold; }
    public int getInterval() { return interval; }
    public int getNumSessions() { return numSessions; }
}