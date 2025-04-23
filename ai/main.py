import cv2
import torch
import pymongo
from insightface.app import FaceAnalysis
from pymongo import MongoClient
import pathlib
import sys
import time
from datetime import datetime
import requests
from face_utils import load_student_embeddings, recognize_face, ensure_dir

# إعداد المسار المحلي لـ YOLOv5
sys.path.append("C:\\Users\\MaysM.M\\yolov5")
from utils.general import scale_boxes

# حل مشكلة توافق المسارات في Windows
pathlib.PosixPath = pathlib.WindowsPath

# تحميل نموذج YOLOv5
model = torch.hub.load("C:/Users/MaysM.M/yolov5", "custom", path="C:\\Users\\MaysM.M\\yolov5\\best.pt", source="local", force_reload=True)
model.conf = 0.25
model.iou = 0.4
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model.to(device)
print(f"Using device: {device}")

# تحميل نموذج ArcFace
app = FaceAnalysis(allowed_modules=['detection', 'recognition'])
app.prepare(ctx_id=0 if torch.cuda.is_available() else -1, det_size=(320, 320))

# الاتصال بـ MongoDB
mongo_client = MongoClient("mongodb://localhost:27017")
db = mongo_client["face_attendance"]
students_collection = db["students_embeddings"]
attendance_collection = db["attendance"]
students_embeddings = load_student_embeddings(students_collection)

# إعداد المجلدات
output_dir = "output"
screenshots_dir = f"{output_dir}/screenshots"
ensure_dir(screenshots_dir)

def detect_faces_from_frame(frame):
    original_shape = frame.shape
    frame_resized = cv2.resize(frame, (1280, 720))
    results = model(frame_resized)
    faces = []
    print(f"[DEBUG] Number of objects detected by YOLO: {len(results.xyxy[0])}")
    
    for *xyxy, conf, cls in results.xyxy[0]:
        x1, y1, x2, y2 = map(int, xyxy)
        x1 = int(x1 * original_shape[1] / 1280)
        y1 = int(y1 * original_shape[0] / 720)
        x2 = int(x2 * original_shape[1] / 1280)
        y2 = int(y2 * original_shape[0] / 720)
        x1, y1 = max(0, x1), max(0, y1)
        x2, y2 = min(original_shape[1], x2), min(original_shape[0], y2)
        
        if x2 - x1 > 0 and y2 - y1 > 0:
            face_location = {"bbox": (x1, y1, x2, y2), "confidence": float(conf)}
            faces.append(face_location)
    return faces

def calculate_sessions(lecture_duration, late_threshold, interval):
    remaining_time = lecture_duration - late_threshold
    num_sessions = max(1, remaining_time // (interval + 1)) + 1  # +1 للجلسة 0
    return num_sessions

def run_camera_for_lecture(course_id, lecture_duration, late_threshold, interval, video_path=None):
    cap = cv2.VideoCapture(video_path if video_path else 0)
    if not cap.isOpened():
        print("Error: Could not open video source.")
        return

    # إنشاء lectureId مؤقت
    lecture_id = f"{course_id}-temp-{datetime.now().strftime('%Y%m%d-%H%M')}"

    # تتبع الاكتشافات لكل جلسة وأول اكتشاف في المحاضرة
    detections = {}  # {session_id: [{"student_id": str, "time": str, "screenshot_path": str}]}
    first_detected_at = {}  # {student_id: first_detected_time}
    total_sessions = calculate_sessions(lecture_duration, late_threshold, interval)
    session_times = [0]  # بداية كل جلسة بالثواني
    for i in range(1, total_sessions):
        if i == 1:
            session_times.append(late_threshold)  # نهاية الجلسة 0
        else:
            session_times.append(session_times[i-1] + interval + 1)

    start_time = time.time()
    current_session = 0

    while True:
        ret, frame = cap.read()
        if not ret:
            print("❌ End of video reached")
            break

        elapsed_time = time.time() - start_time
        if elapsed_time >= lecture_duration:
            print(f"✅ Lecture duration ({lecture_duration} seconds) completed")
            break

        # تحديد الجلسة الحالية بناءً على الوقت
        while current_session < total_sessions - 1 and elapsed_time >= session_times[current_session + 1]:
            current_session += 1
            print(f"[DEBUG] Switching to session {current_session} at {elapsed_time:.2f}s")

        if current_session not in detections:
            detections[current_session] = []

        frame = cv2.resize(frame, (1280, 720))
        faces = detect_faces_from_frame(frame)
        current_time_str = datetime.now().strftime("%H:%M:%S")
        screenshot_path = None

        for face in faces:
            x1, y1, x2, y2 = face["bbox"]
            padding = int(max(x2 - x1, y2 - y1) * 0.5)
            x1 = max(0, x1 - padding)
            y1 = max(0, y1 - padding)
            x2 = min(frame.shape[1], x2 + padding)
            y2 = min(frame.shape[0], y2 + padding)
            face_img = frame[y1:y2, x1:x2]
            if face_img.shape[0] < 40 or face_img.shape[1] < 40:
                continue
            
            face_img_rgb = cv2.cvtColor(face_img, cv2.COLOR_BGR2RGB)
            detected_faces = app.get(face_img_rgb)
            if not detected_faces:
                continue
            
            face_embedding = detected_faces[0].embedding.flatten()
            student_id, similarity = recognize_face(face_embedding, students_embeddings)
            if student_id == "Unknown":
                continue

            # تسجيل أول اكتشاف في المحاضرة
            if student_id not in first_detected_at:
                first_detected_at[student_id] = current_time_str
                print(f"[DEBUG] First detection for {student_id} at {current_time_str}")

            # تسجيل الاكتشاف في الجلسة الحالية (إذا لم يكن مسجلاً بعد)
            if student_id not in [d["student_id"] for d in detections[current_session]]:
                screenshot_path = f"{screenshots_dir}/{lecture_id}_{current_session}_{student_id}.jpg"
                cv2.imwrite(screenshot_path, frame)
                detections[current_session].append({
                    "student_id": student_id,
                    "time": current_time_str,
                    "screenshot_path": screenshot_path
                })
                print(f"✅ Detected {student_id} in session {current_session} at {current_time_str}, similarity: {similarity:.2f}")

            color = (0, 255, 0)
            cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
            cv2.putText(frame, f"{student_id} ({similarity:.2f})", (x1, y1 - 10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

        cv2.imshow("Face Recognition", frame)
        print(f"[DEBUG] Displaying frame at {current_time_str}, Elapsed time: {elapsed_time:.2f}s")
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
        time.sleep(1)  # تحليل كل ثانية

    cap.release()
    cv2.destroyAllWindows()
    save_final_attendance(lecture_id, course_id, detections, first_detected_at, late_threshold, interval, total_sessions, session_times)

def save_final_attendance(lecture_id, course_id, detections, first_detected_at, late_threshold, interval, total_sessions, session_times):
    all_students = students_embeddings.keys()
    session_summary = []

    # طباعة تفاصيل الجلسات
    print("\n=== Session Details ===")
    print(f"Total Sessions: {total_sessions}")
    for i in range(total_sessions):
        start_time = session_times[i]
        end_time = session_times[i + 1] if i < total_sessions - 1 else session_times[i] + (late_threshold if i == 0 else interval + 1)
        print(f"Session {i}: {start_time:.2f}s to {end_time:.2f}s")
        session_summary.append({"session_id": i, "start_time": start_time, "end_time": end_time})

    print("\n=== Attendance Summary ===")
    for student_id in all_students:
        status = "Absent"
        sessions = []
        first_check_times = []

        # تتبع الاكتشافات عبر الجلسات
        for sess_id in range(total_sessions):
            first_detection_time = "undetected"
            first_check_time = "undetected"
            
            for detection in detections.get(sess_id, []):
                if detection["student_id"] == student_id:
                    first_detection_time = detection["time"]
                    first_check_time = detection["time"]
                    if sess_id == 0:  # اكتشاف في الجلسة 0 (قبل lateThreshold)
                        status = "Present"
                    elif status != "Present":  # إذا لم يكن Present، يمكن أن يكون Late
                        status = "Late"
                    break
            
            sessions.append({
                "sessionId": sess_id,
                "firstDetectionTime": first_detection_time
            })
            first_check_times.append({
                "sessionId": sess_id,
                "firstCheckTime": first_check_time
            })

        # إنشاء سجل الحضور
        attendance_record = {
            "lectureId": lecture_id,
            "studentId": student_id,
            "courseId": course_id,
            "status": status,
            "sessions": sessions,
            "firstCheckTimes": first_check_times,
            "firstDetectedAt": first_detected_at.get(student_id, "undetected")  # إضافة أول اكتشاف
        }

        # طباعة ملخص الحضور للطالب
        print(f"Student: {student_id}")
        print(f"  Status: {status}")
        print(f"  First Detected At: {attendance_record['firstDetectedAt']}")
        print("  Sessions:")
        for sess in sessions:
            print(f"    Session {sess['sessionId']}: First Detection = {sess['firstDetectionTime']}")
        print("  First Check Times:")
        for check in first_check_times:
            print(f"    Session {check['sessionId']}: First Check Time = {check['firstCheckTime']}")
        print()

        # حفظ السجل في MongoDB محليًا
        attendance_collection.update_one(
            {"lecture_id": lecture_id, "student_id": student_id},
            {"$set": attendance_record},
            upsert=True
        )
        print(f"✅ Saved final status for {student_id}: {status} in MongoDB")

        # إرسال السجل إلى الـ backend
        try:
            response = requests.post("http://localhost:8080/api/attendances", json=attendance_record)
            if response.status_code == 200:
                response_text = response.text
                if "lectureId" in response_text:
                    new_lecture_id = response_text.split("lectureId: ")[1].replace("\"", "").strip()
                    if new_lecture_id != lecture_id:
                        lecture_id = new_lecture_id
                        attendance_collection.update_one(
                            {"lecture_id": lecture_id, "student_id": student_id},
                            {"$set": {"lectureId": lecture_id}}
                        )
                        print(f"✅ Updated lectureId to {lecture_id} in MongoDB")
                print(f"✅ Saved attendance for {student_id}: {status} - Backend Response: {response.status_code}")
            else:
                print(f"⚠️ Backend returned status {response.status_code} for {student_id}")
        except requests.exceptions.RequestException as e:
            print(f"⚠️ Failed to send attendance for {student_id}: {e}")

    # طباعة ملخص نهائي
    print("\n=== Final Summary ===")
    print(f"Total Students: {len(all_students)}")
    present_count = sum(1 for s in all_students if any(d["student_id"] == s and d["time"] != "undetected" for d in detections.get(0, [])))
    late_count = sum(1 for s in all_students if any(d["student_id"] == s and d["time"] != "undetected" for sess in detections if sess > 0 for d in detections[sess]) and not any(d["student_id"] == s for d in detections.get(0, [])))
    absent_count = len(all_students) - present_count - late_count
    print(f"Present: {present_count}")
    print(f"Late: {late_count}")
    print(f"Absent: {absent_count}")

if __name__ == "__main__":
    run_camera_for_lecture("L001", 300, 10, 15, "C:/Users/MaysM.M/face-attendance-system/8.mp4")