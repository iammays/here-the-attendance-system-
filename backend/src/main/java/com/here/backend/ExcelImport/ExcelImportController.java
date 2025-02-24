package com.here.backend.ExcelImport;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;
import com.here.backend.Camera.CameraEntity;
import com.here.backend.Camera.CameraRepository;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import org.apache.poi.ss.usermodel.*;

@RestController
@RequestMapping("/api/excel")
public class ExcelImportController {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
private CameraRepository cameraRepository; // Injecting the repository



    @PostMapping("/students")
    public String importExcelData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Please upload a valid Excel file.";
        }

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return "Invalid file format. Please upload an Excel (.xlsx) file.";
        }

        System.out.println("Processing file: " + file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<StudentEntity> studentsToSave = new ArrayList<>();
            Set<String> importedStudentIds = new HashSet<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                Cell idCell = row.getCell(0);
                if (idCell == null) {
                    System.out.println("Skipping row: missing student ID");
                    continue;
                }

                String studentId = getStringValue(idCell);
                if (studentId.isEmpty()) {
                    System.out.println("Skipping row: invalid student ID");
                    continue;
                }

                importedStudentIds.add(studentId);
                Optional<StudentEntity> existingStudent = studentRepository.findById(studentId);
                StudentEntity student = existingStudent.orElseGet(StudentEntity::new);
                student.setId(studentId);

                student.setName(getStringValue(row.getCell(1)));
                student.setEmail(getStringValue(row.getCell(2)));
                student.setAdvisorName(getStringValue(row.getCell(3)));

                if (row.getCell(4) != null) {
                    String courseIdsString = getStringValue(row.getCell(4));
                    List<String> courseIds = List.of(courseIdsString.split(","));
                    student.setCourseIds(courseIds);
                }

                if (row.getCell(5) != null) {
                    student.setTotalAbsences(getIntValue(row.getCell(5)));
                } else {
                    student.setTotalAbsences(0);
                }

                studentsToSave.add(student);
            }

            // Delete students from the database if their ID is not in the imported file
            List<String> existingStudentIds = studentRepository.findAll()
                    .stream()
                    .map(StudentEntity::getId)
                    .collect(Collectors.toList());
            
            List<String> studentsToDelete = existingStudentIds.stream()
                    .filter(id -> !importedStudentIds.contains(id))
                    .collect(Collectors.toList());

            studentRepository.deleteAllById(studentsToDelete);
            studentRepository.saveAll(studentsToSave);
            return "Student data imported and updated successfully!";

        } catch (Exception e) {
            System.err.println("Error processing Excel file: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/courses")
    public String importCourses(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Please upload a valid Excel file.";
        }

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return "Invalid file format. Please upload an Excel (.xlsx) file.";
        }

        System.out.println("Processing file: " + file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<CourseEntity> coursesToSave = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String courseId = getStringValue(row.getCell(0));
                String courseName = getStringValue(row.getCell(1));
                String category = getStringValue(row.getCell(2));
                String teacherId = getStringValue(row.getCell(3));
                List<String> roomIds = row.getCell(4) != null ? List.of(getStringValue(row.getCell(4)).split(",")) : new ArrayList<>();

                CourseEntity course = new CourseEntity(courseId, courseName, teacherId, roomIds);
                course.setCategory(category);
                coursesToSave.add(course);
            }

            courseRepository.saveAll(coursesToSave);
            return "Courses imported successfully!";

        } catch (Exception e) {
            System.err.println("Error processing Excel file: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }



    @PostMapping("/cameras")
    public String importCameras(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "Please upload a valid Excel file.";
        }

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return "Invalid file format. Please upload an Excel (.xlsx) file.";
        }

        System.out.println("Processing file: " + file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<CameraEntity> coursesToSave = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String cameraid = getStringValue(row.getCell(0));
                String roomid = getStringValue(row.getCell(1));

                CameraEntity course = new CameraEntity(cameraid, roomid);
                coursesToSave.add(course);
            }

            cameraRepository.saveAll(coursesToSave);
            return "Courses imported successfully!";

        } catch (Exception e) {
            System.err.println("Error processing Excel file: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }


    private String getStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private int getIntValue(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        try {
            return Integer.parseInt(cell.getStringCellValue().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
