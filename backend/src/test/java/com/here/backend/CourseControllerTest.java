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
import org.springframework.security.test.context.support.WithMockUser ;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import com.here.backend.Course.CourseController;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Security.jwt.AuthTokenFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CourseController.class)
class CourseControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CourseRepository courseRepository;

  @MockBean
  private AuthTokenFilter jwtFilter; 

  private CourseEntity course1, course2;
  private final String TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJUZWFjaGVyXzEiLCJpYXQiOjE3NDExOTQ4NjAsImV4cCI6MTc0MTI4MTI2MH0.y-cU-3gLdtz-o8S82QCsZwkf0uPWjQn-uWkbIeSwzR4"; 

  @BeforeEach
  void setUp() {
    course1 = new CourseEntity("M304", "Math", "B-300","T102", "Science", "Monday", "08:00", "10:00");
    course2 = new CourseEntity("PH404", "Physics", "S-300","T104" ,"Science", "Tuesday", "10:00", "12:00");
  }

  @Test
  @WithMockUser
  void getAllCourses_ShouldReturnListOfCourses() throws Exception {
    when(courseRepository.findAll()).thenReturn(Arrays.asList(course1, course2));

    mockMvc.perform(get("/courses")
        .header("Authorization", TOKEN)) 
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.size()").value(2))
    .andExpect(jsonPath("$[0].name").value("Math"))
    .andExpect(jsonPath("$[1].name").value("Physics"));
  }

  @Test
  void getCourseById_ShouldReturnCourse_WhenExists() throws Exception {
    when(courseRepository.findByCourseId("M304")).thenReturn(Optional.of(course1));

    mockMvc.perform(get("/courses/M304")
        .header("Authorization", TOKEN)) 
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.name").value("Math"));
  }

  @Test
  void getCourseById_ShouldReturnNotFound_WhenNotExists() throws Exception {
    when(courseRepository.findByCourseId("99")).thenReturn(Optional.empty());

    mockMvc.perform(get("/courses/99")
        .header("Authorization", TOKEN)) 
    .andExpect(status().isNotFound());
  }

  @Test
  void getCourseByName_ShouldReturnCourses() throws Exception {
    when(courseRepository.findByName("Math")).thenReturn(Arrays.asList(course1));

    mockMvc.perform(get("/courses/name/Math")
        .header("Authorization", TOKEN))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.size()").value(1))
    .andExpect(jsonPath("$[0].name").value("Math"));
  }

  @Test
  void getCoursesByTeacher_ShouldReturnCourses() throws Exception {
    when(courseRepository.findByTeacherId("T102")).thenReturn(Arrays.asList(course1));

    mockMvc.perform(get("/courses/teacher/T102")
        .header("Authorization", TOKEN)) 
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.size()").value(1))
    .andExpect(jsonPath("$[0].name").value("Math"));
  }

  @Test
  void getCourseNameById_ShouldReturnCourseName_WhenExists() throws Exception {
    when(courseRepository.findByCourseId("PH404")).thenReturn(Optional.of(course2));

    mockMvc.perform(get("/courses/PH404/name")
        .header("Authorization", TOKEN)) 
    .andExpect(status().isOk())
    .andExpect(content().string("Physics"));
  }

  @Test
  void getCourseByCategory_ShouldReturnCourses() throws Exception {
    when(courseRepository.findByCategory("Science")).thenReturn(Arrays.asList(course1, course2));

    mockMvc.perform(get("/courses/category/Science")
        .header("Authorization", TOKEN))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.size()").value(2));
  }

  @Test
  void getCourseByNameAndCategory_ShouldReturnCourses() throws Exception {
    when(courseRepository.findByNameAndCategory("Math", "Science")).thenReturn(Arrays.asList(course1));

    mockMvc.perform(get("/courses/name/Math/category/Science")
        .header("Authorization", TOKEN))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.size()").value(1))
    .andExpect(jsonPath("$[0].name").value("Math"));
  }

  @Test
  void getCourseByNameAndTeacher_ShouldReturnCourses() throws Exception {
    when(courseRepository.findByNameAndTeacherId("Math", "T102")).thenReturn(Arrays.asList(course1));

    mockMvc.perform(get("/courses/name/Math/teacher/T102")
        .header("Authorization", TOKEN)) 
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.size()").value(1))
    .andExpect(jsonPath("$[0].name").value("Math"));
  }

  @Test
  void getCourseByDay_ShouldReturnCourses() throws Exception {
    when(courseRepository.findByDay("Monday")).thenReturn(Arrays.asList(course1));

    mockMvc.perform(get("/courses/day/Monday")
        .header("Authorization", TOKEN)) 
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.size()").value(1))
    .andExpect(jsonPath("$[0].name").value("Math"));
  }

  @Test
  void getCourseTimeById_ShouldReturnDurationInMinutes() throws Exception {
    when(courseRepository.findByCourseId("M304")).thenReturn(Optional.of(course1));

    mockMvc.perform(get("/courses/M304/time")
        .header("Authorization", TOKEN)) 
    .andExpect(status().isOk())
    .andExpect(content().string("120"));
  }

  @Test
  void getAllCourses_ShouldReturnEmptyList_WhenNoCourses() throws Exception {
    when(courseRepository.findAll()).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/courses").header("Authorization", TOKEN))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.size()").value(0));
  }

  @Test
  void getCourseByName_ShouldReturnEmptyList_WhenNoMatch() throws Exception {
    when(courseRepository.findByName("UnknownCourse")).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/courses/name/UnknownCourse").header("Authorization", TOKEN))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.size()").value(0));
  }

  @Test
  void getCourseByCategory_ShouldReturnEmptyList_WhenNoMatch() throws Exception {
    when(courseRepository.findByCategory("UnknownCategory")).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/courses/category/UnknownCategory").header("Authorization", TOKEN))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.size()").value(0));
  }

  @Test
  void getCourseByDay_ShouldReturnEmptyList_WhenNoMatch() throws Exception {
    when(courseRepository.findByDay("Sunday")).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/courses/day/Sunday").header("Authorization", TOKEN))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.size()").value(0));
  }

  @Test
  void getCourseTimeById_ShouldReturnNotFound_WhenCourseNotExists() throws Exception {
    when(courseRepository.findByCourseId("99")).thenReturn(Optional.empty());

    mockMvc.perform(get("/courses/99/time").header("Authorization", TOKEN))
    .andExpect(status().isNotFound());
  }
}