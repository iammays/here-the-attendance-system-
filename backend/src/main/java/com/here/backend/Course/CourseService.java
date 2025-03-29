//backend\src\main\java\com\here\backend\Course\CourseService.java

package com.here.backend.Course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

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
}

class CameraSchedule {
    private int lateThreshold;
    private int interval;
    private int numSessions;

    // مُنشئ لجدول الكاميرا
    public CameraSchedule(int lateThreshold, int interval, int numSessions) {
        this.lateThreshold = lateThreshold;
        this.interval = interval;
        this.numSessions = numSessions;
    }

    public int getLateThreshold() { return lateThreshold; }
    public int getInterval() { return interval; }
    public int getNumSessions() { return numSessions; }
}