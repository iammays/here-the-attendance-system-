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
import com.here.backend.Teacher.TeacherEntity;
import com.here.backend.Teacher.TeacherRepository;
import com.here.backend.Camera.CameraEntity;
import com.here.backend.Camera.CameraRepository;
import com.here.backend.Course.CourseEntity;
import com.here.backend.Course.CourseRepository;
import com.here.backend.Room.RoomEntity;
import com.here.backend.Room.RoomRepository;

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
@Autowired
private TeacherRepository TeacherRepository;

@Autowired
private RoomRepository roomRepository;

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
            List<CameraEntity> camerasToSave = new ArrayList<>();
            Set<String> importedCameraIds = new HashSet<>();
    
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
    
                Cell idCell = row.getCell(0);
                Cell roomCell = row.getCell(1);
                
                if (idCell == null || roomCell == null) {
                    System.out.println("Skipping row: missing camera ID or room ID");
                    continue;
                }
    
                String cameraId = getStringValue(idCell);
                String roomId = getStringValue(roomCell);
    
                if (cameraId.isEmpty() || roomId.isEmpty()) {
                    System.out.println("Skipping row: invalid camera ID or room ID");
                    continue;
                }
    
                importedCameraIds.add(cameraId);
                Optional<CameraEntity> existingCamera = cameraRepository.findById(cameraId);
                CameraEntity camera = existingCamera.orElseGet(() -> new CameraEntity(cameraId, roomId));
                camera.setRoomId(roomId); // Ensure roomId is updated if needed
                camerasToSave.add(camera);
            }
    
            // Remove cameras from the database if they are not in the imported file
            List<String> existingCameraIds = cameraRepository.findAll()
                    .stream()
                    .map(CameraEntity::getCameraId)
                    .collect(Collectors.toList());
    
            List<String> camerasToDelete = existingCameraIds.stream()
                    .filter(id -> !importedCameraIds.contains(id))
                    .collect(Collectors.toList());
    
            cameraRepository.deleteAllById(camerasToDelete);
            cameraRepository.saveAll(camerasToSave);
            return "Camera data imported and updated successfully!";
    
        } catch (Exception e) {
            System.err.println("Error processing Excel file: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    

    @PostMapping("/teachers")
    public String importteachers(@RequestParam("file") MultipartFile file) {
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
            List<TeacherEntity> teachersToSave = new ArrayList<>();
            Set<String> importedTeachersIds = new HashSet<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                Cell idCell = row.getCell(0);
                if (idCell == null) {
                    System.out.println("Skipping row: missing student ID");
                    continue;
                }

                String teacherid = getStringValue(idCell);
                if (teacherid.isEmpty()) {
                    System.out.println("Skipping row: invalid student ID");
                    continue;
                }

                importedTeachersIds.add(teacherid);
                Optional<TeacherEntity> existeacher = TeacherRepository.findById(teacherid);
                TeacherEntity teacher = existeacher.orElseGet(TeacherEntity::new);
                teacher.setId(teacherid);

                teacher.setName(getStringValue(row.getCell(1)));
                teacher.setEmail(getStringValue(row.getCell(2)));
                teacher.setPassword(getStringValue(row.getCell(3)));

                if (row.getCell(4) != null) {
                    String courseIdsString = getStringValue(row.getCell(4));
                    List<String> courseIds = List.of(courseIdsString.split(","));
                    teacher.setCourseIds(courseIds);
                }

  

                teachersToSave.add(teacher);
            }

            // Delete students from the database if their ID is not in the imported file
            List<String> existingStudentIds = studentRepository.findAll()
                    .stream()
                    .map(StudentEntity::getId)
                    .collect(Collectors.toList());
            
            List<String> studentsToDelete = existingStudentIds.stream()
                    .filter(id -> !importedTeachersIds.contains(id))
                    .collect(Collectors.toList());

            TeacherRepository.deleteAllById(studentsToDelete);
            TeacherRepository.saveAll(teachersToSave);
            return "Student data imported and updated successfully!";

        } catch (Exception e) {
            System.err.println("Error processing Excel file: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    @PostMapping("/rooms")
    public String importRooms(@RequestParam("file") MultipartFile file) {
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
            List<RoomEntity> roomsToSave = new ArrayList<>();
            Set<String> importedRoomIds = new HashSet<>();
    
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
    
                Cell roomIdCell = row.getCell(0);
                Cell scheduleIdCell = row.getCell(1);
                
                if (roomIdCell == null || scheduleIdCell == null) {
                    System.out.println("Skipping row: missing room ID or schedule ID");
                    continue;
                }
    
                String roomId = getStringValue(roomIdCell);
                String scheduleId = getStringValue(scheduleIdCell);
    
                if (roomId.isEmpty() || scheduleId.isEmpty()) {
                    System.out.println("Skipping row: invalid room ID or schedule ID");
                    continue;
                }
    
                importedRoomIds.add(roomId);
                Optional<RoomEntity> existingRoom = roomRepository.findById(roomId);
                RoomEntity room = existingRoom.orElseGet(RoomEntity::new);
    
                room.setRoomId(roomId); // Ensure ID is set
                room.setScheduleId(scheduleId); // Update schedule ID if changed
                
                roomsToSave.add(room);
            }
    
            // Remove rooms that are no longer present in the imported file
            List<String> existingRoomIds = roomRepository.findAll()
                    .stream()
                    .map(RoomEntity::getRoomId)
                    .collect(Collectors.toList());
    
            List<String> roomsToDelete = existingRoomIds.stream()
                    .filter(id -> !importedRoomIds.contains(id))
                    .collect(Collectors.toList());
    
            if (!roomsToDelete.isEmpty()) {
                roomRepository.deleteAllById(roomsToDelete);
            }
            
            if (!roomsToSave.isEmpty()) {
                roomRepository.saveAll(roomsToSave);
            }
    
            return "Room data imported and updated successfully!";
    
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
