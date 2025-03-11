package com.here.backend.Course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


//for mays dont delete 
@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    // حساب فترات تشغيل الكاميرا
    // public CameraSchedule calculateCameraSchedule(String courseId, int lateThreshold) {
    //     CourseEntity course = courseRepository.findByCourseId(courseId).orElseThrow();
    //     int duration = courseRepository.getCourseTimeById(courseId).getBody();
    //     int remainingTime = duration - 10 - lateThreshold;
    //     int numSessions = duration <= 75 ? 4 : (duration < 120 ? 6 : 8);
    //     int interval = remainingTime / numSessions;

    //     return new CameraSchedule(lateThreshold, interval, numSessions);
    // }
}

class CameraSchedule {
    private int lateThreshold;  // دقايق التأخير
    private int interval;       // الفترة بين الجلسات
    private int numSessions;    // عدد الجلسات

    public CameraSchedule(int lateThreshold, int interval, int numSessions) {
        this.lateThreshold = lateThreshold;
        this.interval = interval;
        this.numSessions = numSessions;
    }

    // Getters
    public int getLateThreshold() { return lateThreshold; }
    public int getInterval() { return interval; }
    public int getNumSessions() { return numSessions; }
}