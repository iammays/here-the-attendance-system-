package com.here.backend;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;
import com.here.backend.Course.CourseRepository;
import com.here.backend.ExcelImport.ExcelImportController;

@ExtendWith(MockitoExtension.class)
public class ExcelImportControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ExcelImportController excelImportController;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

   

    @Mock
    private MultipartFile file;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(excelImportController).build();
        lenient().when(file.isEmpty()).thenReturn(false);
        lenient().when(file.getOriginalFilename()).thenReturn("/com/here/backend/resources/Students.xlsx");
    }

    @Test
    void testImportExcelData_InvalidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "Students.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                new byte[0]
        );
    
        mockMvc.perform(multipart("/api/excel/students")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testImportExcelData_Success() throws Exception {
        // تحميل ملف Excel من الموارد
        InputStream inputStream = getClass().getResourceAsStream("/com/here/backend/resources/Students.xlsx");
        assertNotNull(inputStream, "Test file not found in resources"); // تأكيد أن الملف موجود
        
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", 
                "Students.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                inputStream
        );
    
        StudentEntity student = new StudentEntity();
        student.setStudentId("123");
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        System.out.println("Mocked student found: " + studentRepository.findById("123").isPresent());

        // تنفيذ الطلب باستخدام MockMvc
        mockMvc.perform(multipart("/api/excel/students")
                .file(mockFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Student data imported and updated successfully!"));
                // .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void testUploadCourses_Success() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/com/here/backend/resources/Courses.xlsx");
        assertNotNull(inputStream, "Test file not found in resources");
        
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", 
                "Courses.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                inputStream
        );
    
        mockMvc.perform(multipart("/api/excel/courses")
                .file(mockFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Course uploaded successfully!")));
    }

    @Test
    void testUploadCourses_InvalidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "Courses.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                new byte[0]
        );
    
        mockMvc.perform(multipart("/api/excel/courses")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
                // .andExpect(content().string("الملف فارغ!"));
    }

    @Test
    void testImportCameras_Success() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/com/here/backend/resources/Camera.xlsx");
        assertNotNull(inputStream, "Test file not found in resources");
        
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", 
                "Cameras.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                inputStream
        );
    
        mockMvc.perform(multipart("/api/excel/cameras")
                .file(mockFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Camera data imported and updated successfully!")));
    }

    @Test
    void testImportCameras_InvalidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "Camera.xlsx", 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                new byte[0]
        );
    
        mockMvc.perform(multipart("/api/excel/cameras")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                // .andExpect(status().isBadRequest())
                .andExpect(content().string("Please upload a valid Excel file."));
}
}