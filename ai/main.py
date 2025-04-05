# ai/main.py
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
    num_sessions = max(1, remaining_time // (interval + 1)) + 1  # +1 عشان Session 0
    return num_sessions

def run_camera_for_lecture(lecture_id, lecture_duration, late_threshold, interval, video_path=None):
    cap = cv2.VideoCapture(video_path if video_path else 0)
    if not cap.isOpened():
        print("Error: Could not open video source.")
        return

    detections = {}
    total_sessions = calculate_sessions(lecture_duration, late_threshold, interval)
    session_times = [0]  # بداية كل جلسة بالثواني
    for i in range(1, total_sessions):
        if i == 1:
            session_times.append(late_threshold)  # نهاية Session 0
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
            print(f"[DEBUG] Switching to session {current_session}")

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
            if student_id != "Unknown" and student_id not in [d["student_id"] for d in detections.get(current_session, [])]:
                screenshot_path = f"{screenshots_dir}/{lecture_id}_{current_session}_{student_id}.jpg"
                cv2.imwrite(screenshot_path, frame)
                detections[current_session].append({
                    "student_id": student_id,
                    "time": current_time_str,
                    "screenshot_path": screenshot_path
                })
                print(f"✅ Detected {student_id} in session {current_session} at {current_time_str}, similarity: {similarity:.2f}")

            color = (0, 255, 0) if student_id != "Unknown" else (0, 0, 255)
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
    save_final_attendance(lecture_id, detections, late_threshold, total_sessions)

def save_final_attendance(lecture_id, detections, late_threshold, total_sessions):
    all_students = students_embeddings.keys()
    for student_id in all_students:
        status = "Absent"
        sessions = []

        for sess_id in range(total_sessions):
            first_detection_time = "undetected"
            for detection in detections.get(sess_id, []):
                if detection["student_id"] == student_id:
                    first_detection_time = detection["time"]
                    if sess_id == 0:
                        status = "Present"
                    else:
                        status = "Late"
                    break
            sessions.append({"sessionId": sess_id, "firstDetectionTime": first_detection_time})

        attendance_record = {
            "lectureId": lecture_id,
            "studentId": student_id,
            "status": status,
            "sessions": sessions
        }

        attendance_collection.update_one(
            {"lecture_id": lecture_id, "student_id": student_id},
            {"$set": attendance_record},
            upsert=True
        )
        print(f"✅ Saved final status for {student_id}: {status} in MongoDB")

        try:
            response = requests.post("http://localhost:8080/api/attendances", json=attendance_record)
            print(f"✅ Saved attendance for {student_id}: {status} - Backend Response: {response.status_code}")
        except requests.exceptions.RequestException as e:
            print(f"⚠️ Failed to send attendance for {student_id}: {e}")

if __name__ == "__main__":
    run_camera_for_lecture("L001", 300, 10, 15, "C:/Users/MaysM.M/face-attendance-system/8.mp4")