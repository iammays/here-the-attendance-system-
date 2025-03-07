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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Security.security.services.UserDetailsImpl;
import com.here.backend.Teacher.TeacherController;
import com.here.backend.Teacher.TeacherEntity;
import com.here.backend.Teacher.TeacherRepository;
import java.util.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TeacherController.class)
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeacherRepository teacherRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private PasswordEncoder encoder;

    private TeacherEntity teacher1, teacher2;
    private CourseEntity course1, course2;
    private final String TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJUZWFjaGVyXzEiLCJpYXQiOjE3NDExOTU1MjcsImV4cCI6MTc0MTI4MTkyN30.tgNsEKYNNGLlHr3v0Qh7dAwzL225O5b_3hxacIH82fg";

    @BeforeEach
    void setUp() {
        teacher1 = new TeacherEntity("John Doe", "john@gmail.com", "password1");
        teacher2 = new TeacherEntity("Jane Doe", "jane@edu.com", "password2");
        course1 = new CourseEntity("C1", "Math", "B-300", "T102", "Science", "Monday", "08:00", "10:00");
        course2 = new CourseEntity("C2", "Physics", "S-300", "T104", "Science", "Tuesday", "10:00", "12:00");
    }

    @Test
    void getAllTeachers_ShouldReturnListOfTeachers() throws Exception {
        when(teacherRepository.findAll()).thenReturn(Arrays.asList(teacher1, teacher2));

        mockMvc.perform(get("/teachers")
                .header("Authorization", TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getTeacherById_ShouldReturnTeacher_WhenExists() throws Exception {
        when(teacherRepository.findByTeacherId("1")).thenReturn(Optional.of(teacher1));

        mockMvc.perform(get("/teachers/Teacherid/1")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getTeacherById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(teacherRepository.findByTeacherId("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/teachers/Teacherid/99")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isNotFound());
    }

    @Test
    void getTeacherByName_ShouldReturnTeachers_WhenExists() throws Exception {
        when(teacherRepository.findByNameContainingIgnoreCase("John")).thenReturn(Arrays.asList(teacher1));

        mockMvc.perform(get("/teachers/name/John")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void getTeacherByName_ShouldReturnNotFound_WhenNoMatch() throws Exception {
        when(teacherRepository.findByNameContainingIgnoreCase("Unknown")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/teachers/name/Unknown")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isNotFound())
            .andExpect(content().string("No teachers found with the given name."));
    }

    @Test
    void getTeachersByCourseId_ShouldReturnTeachers_WhenExists() throws Exception {
        when(teacherRepository.findByCourseId("C1")).thenReturn(new HashSet<>(Arrays.asList(teacher1)));

        mockMvc.perform(get("/teachers/course/C1/all")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void getTeachersByCourseId_ShouldReturnNotFound_WhenNoMatch() throws Exception {
        when(teacherRepository.findByCourseId("UnknownCourse")).thenReturn(Collections.emptySet());

        mockMvc.perform(get("/teachers/course/UnknownCourse/all")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isNotFound())
            .andExpect(content().string("No teachers found for this course."));
    }

    @Test
    void getTeacherByEmail_ShouldReturnTeacherAndCourses_WhenExists() throws Exception {
        when(teacherRepository.findByEmail("john@example.com")).thenReturn(Optional.of(teacher1));
        when(courseRepository.findByTeacherId("1")).thenReturn(Arrays.asList(course1, course2));

        mockMvc.perform(get("/teachers/email/john@example.com")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.teacher.name").value("John Doe"))
            .andExpect(jsonPath("$.courses.size()").value(2));
    }

    @Test
    void getTeacherByEmail_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(teacherRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/teachers/email/unknown@example.com")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isNotFound())
            .andExpect(content().string("Teacher not found"));
    }

    @Test
    void getAllCoursesForTeacher_ShouldReturnCourses_WhenTeacherExists() throws Exception {
        when(teacherRepository.findByTeacherId("1")).thenReturn(Optional.of(teacher1));
        when(courseRepository.findByTeacherId("1")).thenReturn(Arrays.asList(course1, course2));

        mockMvc.perform(get("/teachers/courses/1")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getAllCoursesForTeacher_ShouldReturnNotFound_WhenTeacherNotExists() throws Exception {
        when(teacherRepository.findByTeacherId("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/teachers/courses/99")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isNotFound())
            .andExpect(content().string("Teacher not found"));
    }

    @Test
    void getTeacherWithCourses_ShouldReturnTeacherAndCourses_WhenExists() throws Exception {
        when(teacherRepository.findByTeacherId("1")).thenReturn(Optional.of(teacher1));
        when(courseRepository.findByTeacherId("1")).thenReturn(Arrays.asList(course1, course2));

        mockMvc.perform(get("/teachers/1/courses")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.teacher.name").value("John Doe"))
            .andExpect(jsonPath("$.courses.size()").value(2));
    }

    @Test
    void getTeacherWithCourses_ShouldReturnNotFound_WhenTeacherNotExists() throws Exception {
        when(teacherRepository.findByTeacherId("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/teachers/99/courses")
                .header("Authorization", TOKEN)) // إضافة التوكن هنا
            .andExpect(status().isNotFound())
            .andExpect(content().string("Teacher not found"));
    }

    @Test
    void updateTeacherPassword_ShouldUpdatePassword_WhenValid() throws Exception {
        when(teacherRepository.findByTeacherId("1")).thenReturn(Optional.of(teacher1));
        when(encoder.matches("oldPassword", teacher1.getPassword())).thenReturn(true);
        when(teacherRepository.save(any(TeacherEntity.class))).thenReturn(teacher1);

        mockMvc.perform(put("/teachers/1/password")
                .header("Authorization", TOKEN) // إضافة التوكن هنا
                .contentType("application/json")
                .content("{\"oldPassword\":\"oldPassword\", \"newPassword\":\"newPassword\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("Password updated successfully."));
    }

    @Test
    void updateTeacherPassword_ShouldReturnForbidden_WhenNotAuthorized() throws Exception {
        UserDetailsImpl currentUser  = mock(UserDetailsImpl.class);
        when(currentUser .getId()).thenReturn("2"); // ID مختلف عن المعلم

        mockMvc.perform(put("/teachers/1/password")
                .header("Authorization", TOKEN) // إضافة التوكن هنا
                .contentType("application/json")
                .content("{\"oldPassword\":\"oldPassword\", \"newPassword\":\"newPassword\"}"))
            .andExpect(status().isForbidden())
            .andExpect(content().string("Error: You are not allowed to update this password."));
    }

    @Test
    void updateTeacherPassword_ShouldReturnNotFound_WhenTeacherNotExists() throws Exception {
        when(teacherRepository.findByTeacherId("99")).thenReturn(Optional.empty());

        mockMvc.perform(put("/teachers/99/password")
                .header("Authorization", TOKEN) // إضافة التوكن هنا
                .contentType("application/json")
                .content("{\"oldPassword\":\"oldPassword\", \"newPassword\":\"newPassword\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateTeacherPassword_ShouldReturnBadRequest_WhenOldPasswordIncorrect() throws Exception {
        when(teacherRepository.findByTeacherId("1")).thenReturn(Optional.of(teacher1));
        when(encoder.matches("wrongOldPassword", teacher1.getPassword())).thenReturn(false);

        mockMvc.perform(put("/teachers/1/password")
                .header("Authorization", TOKEN) // إضافة التوكن هنا
                .contentType("application/json")
                .content("{\"oldPassword\":\"wrongOldPassword\", \"newPassword\":\"newPassword\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error: Incorrect old password."));
    }
}