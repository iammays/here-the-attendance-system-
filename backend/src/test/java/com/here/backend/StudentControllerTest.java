package com.here.backend;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Student.StudentController;
import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;
import java.util.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private CourseRepository courseRepository;

    private StudentEntity student1, student2;
    private CourseEntity course1, course2;
    private final String TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJUZWFjaGVyXzEiLCJpYXQiOjE3NDExOTU1MjcsImV4cCI6MTc0MTI4MTkyN30.tgNsEKYNNGLlHr3v0Qh7dAwzL225O5b_3hxacIH82fg";

    @BeforeEach
    void setUp() {
        student1 = new StudentEntity("1", "John Doe", "john@example.com","Teacher_1", Arrays.asList("C1", "C2"));
        student2 = new StudentEntity("2", "Jane Doe", "jane@example.com","Teacher_3", Collections.singletonList("C1"));
        course1 = new CourseEntity("C1", "Math", "B-300", "T102", "Science", "Monday", "08:00", "10:00");
        course2 = new CourseEntity("C2", "Physics", "S-300", "T104", "Science", "Tuesday", "10:00", "12:00");
    }

    @Test
    void getStudentById_ShouldReturnStudent_WhenExists() throws Exception {
        when(studentRepository.findByStudentId("1")).thenReturn(Optional.of(student1));

        mockMvc.perform(get("/students/id/1")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getStudentById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(studentRepository.findByStudentId("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/students/id/99")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isNotFound());
    }

    @Test
    void getStudentByName_ShouldReturnStudents_WhenExists() throws Exception {
        when(studentRepository.findByName("John Doe")).thenReturn(Arrays.asList(student1));

        mockMvc.perform(get("/students/name/John Doe")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void getStudentByName_ShouldReturnNotFound_WhenNoMatch() throws Exception {
        when(studentRepository.findByName("Unknown")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/students/name/Unknown")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isNotFound());
    }

    @Test
    void getStudentByEmail_ShouldReturnStudent_WhenExists() throws Exception {
        when(studentRepository.findByEmail("john@example.com")).thenReturn(Arrays.asList(student1));

        mockMvc.perform(get("/students/email/john@example.com")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getStudentByEmail_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(studentRepository.findByEmail("unknown@example.com")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/students/email/unknown@example.com")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isNotFound());
    }

    @Test
    void getAllStudents_ShouldReturnListOfStudents() throws Exception {
        when(studentRepository.findAll()).thenReturn(Arrays.asList(student1, student2));

        mockMvc.perform(get("/students")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getAllStudents_ShouldReturnEmptyList_WhenNoStudents() throws Exception {
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/students")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void getStudentsByCourse_ShouldReturnStudents_WhenExists() throws Exception {
        when(studentRepository.findByCourseId("C1")).thenReturn(Arrays.asList(student1, student2));

        mockMvc.perform(get("/students/course/C1")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getStudentsByCourse_ShouldReturnEmptyList_WhenNoMatch() throws Exception {
        when(studentRepository.findByCourseId("UnknownCourse")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/students/course/UnknownCourse")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void getAllCoursesForStudent_ShouldReturnCourses_WhenStudentExists() throws Exception {
        when(studentRepository.findByStudentId("1")).thenReturn(Optional.of(student1));
        when(courseRepository.findByCourseIdIn(Arrays.asList("C1", "C2"))).thenReturn(Arrays.asList(course1, course2));

        mockMvc.perform(get("/students/1/courses")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getAllCoursesForStudent_ShouldReturnEmptyList_WhenNoCourses() throws Exception {
        when(studentRepository.findByStudentId("1")).thenReturn(Optional.of(student1));
        when(courseRepository.findByCourseIdIn(Arrays.asList("C1", "C2"))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/students/1/courses")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void getAllCoursesForStudent_ShouldReturnEmptyList_WhenStudentNotExists() throws Exception {
        when(studentRepository.findByStudentId("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/students/99/courses")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void getStudentsByAdvisor_ShouldReturnStudents_WhenExists() throws Exception {
        when(studentRepository.findByAdvisor("Advisor1")).thenReturn(Arrays.asList(student1));

        mockMvc.perform(get("/students/advisor/Advisor1")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void getStudentsByAdvisor_ShouldReturnEmptyList_WhenNoMatch() throws Exception {
        when(studentRepository.findByAdvisor("UnknownAdvisor")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/students/advisor/UnknownAdvisor")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void createStudent_ShouldCreateStudent() throws Exception {
        when(studentRepository.save(any(StudentEntity.class))).thenReturn(student1);

        mockMvc.perform(post("/students")
                .header("Authorization", TOKEN) // إضافة التوكن هنا
                .contentType("application/json")
                .content("{\"id\":\"1\",\"name\":\"John Doe\",\"email\":\"john@example.com\",\"courseId\":[\"C1\",\"C2\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void addStudentInCourse_ShouldAddStudentToCourse_WhenStudentExists() throws Exception {
        when(studentRepository.findByStudentId("1")).thenReturn(Optional.of(student1));

        mockMvc.perform(post("/students/course/C1/students/1")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(content().string("Student enrolled in course successfully."));
    }

    @Test
    void addStudentInCourse_ShouldReturnNotFound_WhenStudentNotExists() throws Exception {
        when(studentRepository.findByStudentId("99")).thenReturn(Optional.empty());

        mockMvc.perform(post("/students/course/C1/students/99")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isNotFound())
            .andExpect(content().string("Student not found."));
    }

    @Test
    void addStudentInCourse_ShouldReturnBadRequest_WhenAlreadyEnrolled() throws Exception {
        when(studentRepository.findByStudentId("1")).thenReturn(Optional.of(student1));

        mockMvc.perform(post("/students/course/C1/students/1")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Student is already enrolled in this course."));
    }
}