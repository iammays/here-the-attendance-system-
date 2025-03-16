package com.here.backend.ExcelImport;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/excel")
public class ExcelImportController {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CameraRepository cameraRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/students")
    public ResponseEntity<?> importExcelData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid or empty file. Please upload a valid Excel (.xlsx) file."));
        }

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<StudentEntity> studentsToSave = new ArrayList<>();
            Set<String> importedStudentIds = new HashSet<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String studentId = getStringValue(row.getCell(0));
                if (studentId.isEmpty()) continue;

                importedStudentIds.add(studentId);
                Optional<StudentEntity> existingStudent = studentRepository.findById(studentId);
                StudentEntity student = existingStudent.orElseGet(StudentEntity::new);
                student.setStudentId(studentId);
                student.setName(getStringValue(row.getCell(1)));
                student.setEmail(getStringValue(row.getCell(2)));
                student.setAdvisor(getStringValue(row.getCell(3)));

                if (row.getCell(4) != null) {
                    String courseIdsString = getStringValue(row.getCell(4));
                    student.setCourseId(List.of(courseIdsString.split(",")));
                }

                studentsToSave.add(student);
            }

            studentRepository.deleteAllById(studentRepository.findAll().stream()
                .map(StudentEntity::getStudentId)
                .filter(id -> !importedStudentIds.contains(id))
                .collect(Collectors.toList()));

            studentRepository.saveAll(studentsToSave);
            return ResponseEntity.ok(Collections.singletonMap("message", "Student data imported and updated successfully!"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/courses")
    public ResponseEntity<String> uploadCourses(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("الملف فارغ!");
        }

        List<CourseEntity> courses = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // أخذ أول شيت في الملف
            Iterator<Row> rowIterator = sheet.iterator();
            boolean firstRow = true;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                if (firstRow) { // تخطي الصف الأول إذا كان يحتوي على العناوين
                    firstRow = false;
                    continue;
                }

                // استخراج البيانات من كل خلية
                String courseId = getStringValue(row.getCell(0));
                String name = getStringValue(row.getCell(1));
                String roomId = getStringValue(row.getCell(2));
                String teacherId = getStringValue(row.getCell(3));
                String startTime = getStringValue(row.getCell(4));
                String endTime = getStringValue(row.getCell(5));
                String day = getStringValue(row.getCell(6));
                String category = getStringValue(row.getCell(7));
                int credits = getIntValue(row.getCell(8));

                // التأكد من أن جميع القيم ليست فارغة
                if (courseId.isEmpty() || name.isEmpty() || roomId.isEmpty() || teacherId.isEmpty()) {
                    continue;
                }

                CourseEntity course = new CourseEntity(courseId, name, roomId, teacherId, startTime, endTime, day, category,credits);
                courses.add(course);
            }

            courseRepository.saveAll(courses);
            return ResponseEntity.ok("Course uploaded successfully!" + courses.size());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while processing the file.: " + e.getMessage());
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
    public ResponseEntity<?> importTeachers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid or empty file. Please upload a valid Excel (.xlsx) file."));
        }

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<TeacherEntity> teachersToSave = new ArrayList<>();
            Set<String> importedTeacherIds = new HashSet<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String teacherId = getStringValue(row.getCell(0));
                if (teacherId.isEmpty()) continue;

                importedTeacherIds.add(teacherId);
                Optional<TeacherEntity> existingTeacher = teacherRepository.findById(teacherId);
                TeacherEntity teacher = existingTeacher.orElseGet(TeacherEntity::new);
                teacher.setTeacherId(teacherId);
                teacher.setName(getStringValue(row.getCell(1)));
                teacher.setEmail(getStringValue(row.getCell(2)));

                // 🔐 تشفير كلمة المرور إذا كانت جديدة أو غير موجودة سابقًا
                String rawPassword = getStringValue(row.getCell(3));
                if (!rawPassword.isEmpty()) {
                    teacher.setPassword(passwordEncoder.encode(rawPassword));
                }

                if (row.getCell(4) != null) {
                    String courseIdsString = getStringValue(row.getCell(4));
                    teacher.setCourseId(List.of(courseIdsString.split(",")));
                }

                teachersToSave.add(teacher);
            }

            // حذف المدرسين غير الموجودين في الملف
            teacherRepository.deleteAllById(teacherRepository.findAll().stream()
                    .map(TeacherEntity::getTeacherId)
                    .filter(id -> !importedTeacherIds.contains(id))
                    .collect(Collectors.toList()));

            teacherRepository.saveAll(teachersToSave);
            return ResponseEntity.ok(Collections.singletonMap("message", "Teacher data imported and updated successfully!"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
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
                Cell Course_idCell = row.getCell(1);

                if (roomIdCell == null || Course_idCell == null) {
                    System.out.println("Skipping row: missing room ID or schedule ID");
                    continue;
                }

                String roomId = getStringValue(roomIdCell);
                String Course_id = getStringValue(Course_idCell);

                if (roomId.isEmpty() || Course_id.isEmpty()) {
                    System.out.println("Skipping row: invalid room ID or schedule ID");
                    continue;
                }

                importedRoomIds.add(roomId);
                Optional<RoomEntity> existingRoom = roomRepository.findById(roomId);
                RoomEntity room = existingRoom.orElseGet(RoomEntity::new);

                room.setRoom_id(roomId); // Ensure ID is set
                room.setCourse_id(Course_id); // Update schedule ID if changed

                roomsToSave.add(room);
            }

            // Remove rooms that are no longer present in the imported file
            List<String> existingRoomIds = roomRepository.findAll()
            .stream()
            .map(RoomEntity::getRoom_id)
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
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
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