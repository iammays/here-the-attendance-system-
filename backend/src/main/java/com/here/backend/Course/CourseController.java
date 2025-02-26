package com.here.backend.Course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private StudentRepository studentRepository;
    
        public CourseController(CourseRepository courseRepository) {
            this.courseRepository = courseRepository;
        }
    
        // get all courses ✅
        // get all students in a course
        // get all courses for a student
        // get all courses for a teacher
        // get all courses for a department
        // get all courses for a semester
        // get course by id ✅
        // get course by name ✅
        // get courses by teacher 
        // get course name by id 
        // get course by category 
        // get course by name and category 
        // get course by name and teacher 
        // create course 
        // update course by id 
        // delete course by id 
    
        @GetMapping
        public List<CourseEntity> getAllCourses() {
            return courseRepository.findAll();
        }
    
        @GetMapping("/{id}/students")
        public ResponseEntity<List<StudentEntity>> getStudentsInCourse(@PathVariable String id) {
            List<StudentEntity> students = studentRepository.findByCourseId(id);
        return ResponseEntity.ok(students);
    }

    // @GetMapping("/student/{studentId}")
    // public ResponseEntity<List<CourseEntity>> getCoursesForStudent(@PathVariable String studentId) {
    //     List<CourseEntity> courses = courseRepository.findByStudentIds(studentId);
    //     return ResponseEntity.ok(courses);
    // }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseEntity>> getCoursesByTeacher(@PathVariable String teacherId) {
        List<CourseEntity> courses = courseRepository.findByTeacherId(teacherId);
        return ResponseEntity.ok(courses);
    }

    // @GetMapping("/department/{departmentId}")
    // public ResponseEntity<List<CourseEntity>> getCoursesForDepartment(@PathVariable String departmentId) {
    //     List<CourseEntity> courses = courseRepository.findByDepartment(departmentId);
    //     return ResponseEntity.ok(courses);
    // }

    // @GetMapping("/semester/{semester}")
    // public ResponseEntity<List<CourseEntity>> getCoursesForSemester(@PathVariable String semester) {
    //     List<CourseEntity> courses = courseRepository.findBySemester(semester);
    //     return ResponseEntity.ok(courses);
    // }

    @GetMapping("/{id}")
    public ResponseEntity<CourseEntity> getCourseById(@PathVariable String id) {
        return courseRepository.findByCourseId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{courseName}")
    public ResponseEntity<List<CourseEntity>> getCourseByName(@PathVariable String courseName) {
        List<CourseEntity> courses = courseRepository.findByName(courseName);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseEntity>> getCourseByCategory(@PathVariable String category) {
        List<CourseEntity> courses = courseRepository.findByCategory(category);
        return ResponseEntity.ok(courses);
    }

    // @GetMapping("/{id}/category")
    // public ResponseEntity<?> getCourseCategory(@PathVariable String id) {
    //     return courseRepository.findById(id)
    //         .map(course -> ResponseEntity.ok(course.getCategory()))
    //         .orElseGet(() -> ResponseEntity.notFound().build());
    // }

    // @GetMapping("/{id}/name")
    // public ResponseEntity<?> getCourseNameById(@PathVariable String id) {
    //     return courseRepository.findById(id)
    //         .map(course -> ResponseEntity.ok(course.getCourseName()))
    //         .orElseGet(() -> ResponseEntity.notFound().build());
    // }


    @GetMapping("/name/{courseName}/category/{category}")
    public ResponseEntity<List<CourseEntity>> getCourseByNameAndCategory(@PathVariable String courseName, @PathVariable String category) {
        List<CourseEntity> courses = courseRepository.findByNameAndCategory(courseName, category);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/name/{courseName}/teacher/{teacherId}")
    public ResponseEntity<List<CourseEntity>> getCourseByNameAndTeacher(@PathVariable String courseName, @PathVariable String teacherId) {
        List<CourseEntity> courses = courseRepository.findByNameAndTeacherId(courseName, teacherId);
        return ResponseEntity.ok(courses);
    }
    // Create a new course
    // @PostMapping
    // public CourseEntity createCourse(@RequestBody CourseEntity courseEntity) {
    //     return courseRepository.save(courseEntity);
    // }

    @PostMapping
    public ResponseEntity<CourseEntity> addCourse(@RequestBody CourseEntity course) {
        CourseEntity savedCourse = courseRepository.save(course);
        return ResponseEntity.ok(savedCourse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseEntity> updateCourse(@PathVariable String id, @RequestBody CourseEntity updatedCourse) {
        Optional<CourseEntity> courseData = courseRepository.findByCourseId(id);
        if (courseData.isPresent()) {
            CourseEntity course = courseData.get();
            course.setName(updatedCourse.getName());
            // course.setCategory(updatedCourse.getCategory());
            course.setTeacherId(updatedCourse.getTeacherId());
            courseRepository.save(course);
            return ResponseEntity.ok(course);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable String id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return ResponseEntity.ok("Course deleted successfully.");
        }
        return ResponseEntity.notFound().build();
    }
}
//     // Update a course
//     @PutMapping("/{id}")
//     public ResponseEntity<CourseEntity> updateCourse(@PathVariable String id, @RequestBody CourseEntity updatedCourse) {
//         Optional<CourseEntity> courseData = courseRepository.findById(id);
//         if (courseData.isPresent()) {
//             CourseEntity course = courseData.get();
//             course.setCourseName(updatedCourse.getCourseName());
//             course.setCategory(updatedCourse.getCategory());
//             course.setTeacherId(updatedCourse.getTeacherId());
//             courseRepository.save(course);
//             return ResponseEntity.ok(course);
//         }
//         return ResponseEntity.notFound().build();
//     }

//     // Get all courses
//     @GetMapping
//     public List<CourseEntity> getAllCourses() {
//         return courseRepository.findAll();
//     }

//     // Get a course by ID
//     @GetMapping("/{id}")
//     public ResponseEntity<CourseEntity> getCourseById(@PathVariable String id) {
//         return courseRepository.findById(id)
//             .map(ResponseEntity::ok)
//             .orElse(ResponseEntity.notFound().build());
//     }

//     // // Delete a course
//     // @DeleteMapping("/{id}")
//     // public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
//     //     return courseRepository.findById(id)
//     //             .map(course -> {
//     //                 courseRepository.delete(course);
//     //                 return ResponseEntity.ok().<Void>build();
//     //             })
//     //             .orElse(ResponseEntity.notFound().build());
//     // }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<?> deleteCourse(@PathVariable String id) {
//         if (courseRepository.existsById(id)) {
//             courseRepository.deleteById(id);
//             return ResponseEntity.ok("Course deleted successfully.");
//         }
//         return ResponseEntity.notFound().build();
//     }

//     @GetMapping("/{id}/category")
//     public ResponseEntity<?> getCourseCategory(@PathVariable String id) {
//         return courseRepository.findById(id)
//             .map(course -> ResponseEntity.ok(course.getCategory()))
//             .orElseGet(() -> ResponseEntity.notFound().build());
//     }

//     @GetMapping("/{id}/name")
//     public ResponseEntity<?> getCourseName(@PathVariable String id) {
//         return courseRepository.findById(id)
//             .map(course -> ResponseEntity.ok(course.getCourseName()))
//             .orElseGet(() -> ResponseEntity.notFound().build());
//     }
// }
