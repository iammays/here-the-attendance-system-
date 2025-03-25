import cv2
import torch
import numpy as np
import pymongo
from insightface.app import FaceAnalysis
from pymongo import MongoClient
import pathlib
import sys
import time
from datetime import datetime
import requests

# إعداد المسار المحلي لـ YOLOv5
sys.path.append("C:\\Users\\MaysM.M\\yolov5")
from utils.general import scale_boxes
from face_utils import load_student_embeddings, recognize_face, ensure_dir

# حل مشكلة توافق المسارات في Windows
pathlib.PosixPath = pathlib.WindowsPath

# تحميل نموذج YOLOv5
model = torch.hub.load("C:/Users/MaysM.M/yolov5", "custom", path="C:\\Users\\MaysM.M\\yolov5\\best.pt", source="local", force_reload=True)
model.conf = 0.6  
model.iou = 0.4
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model.to(device)
print(f"Using device: {device}")

# تحميل نموذج ArcFace
app = FaceAnalysis(allowed_modules=['detection', 'recognition'])
app.prepare(ctx_id=0 if torch.cuda.is_available() else -1, det_size=(640, 640))  # زيادة det_size لدقة أعلى

# الاتصال بـ MongoDB
mongo_client = MongoClient("mongodb://localhost:27017")
db = mongo_client["face_attendance"]
students_collection = db["students_embeddings"]
attendance_collection = db["attendance"]
students_embeddings = load_student_embeddings(students_collection)

# إعداد المجلدات
output_dir = "output"
screenshots_dir = f"{output_dir}/screenshots"
known_faces_dir = f"{output_dir}/known_faces"
unknown_faces_dir = f"{output_dir}/unknown_faces"
yolo_cropped_dir = f"{output_dir}/yolo_cropped_faces"
ensure_dir(screenshots_dir)
ensure_dir(known_faces_dir)
ensure_dir(unknown_faces_dir)
ensure_dir(yolo_cropped_dir)

def detect_faces_from_frame(frame):
    original_shape = frame.shape
    frame_resized = cv2.resize(frame, (640, 480))
    results = model(frame_resized)
    faces = []
    print(f"[DEBUG] Number of objects detected by YOLO: {len(results.xyxy[0])}")
    
    for *xyxy, conf, cls in results.xyxy[0]:
        x1, y1, x2, y2 = map(int, xyxy)
        x1 = int(x1 * original_shape[1] / 640)
        y1 = int(y1 * original_shape[0] / 480)
        x2 = int(x2 * original_shape[1] / 640)
        y2 = int(y2 * original_shape[0] / 480)
        x1, y1 = max(0, x1), max(0, y1)
        x2, y2 = min(original_shape[1], x2), min(original_shape[0], y2)
        
        if x2 - x1 > 0 and y2 - y1 > 0:
            face_location = {"bbox": (x1, y1, x2, y2), "confidence": float(conf)}
            faces.append(face_location)
    return faces

def recognize_faces_session(lecture_id, session_id, duration, video_path, detections):
    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        print(f"❌ Failed to open video: {video_path}")
        return
    
    start_time = time.time()
    frame_count = 0
    last_screenshot_time = 0
    screenshot_interval = 1
    
    while cap.isOpened() and (time.time() - start_time) < duration:
        ret, frame = cap.read()
        if not ret or frame is None:
            print("❌ End of video or error reading frame.")
            break
        
        frame = cv2.resize(frame, (640, 480))
        frame_count += 1
        current_time = time.time()

        if current_time - last_screenshot_time >= screenshot_interval:
            screenshot_path = f"{screenshots_dir}/frame_{session_id}_{frame_count}.jpg"
            cv2.imwrite(screenshot_path, frame)
            last_screenshot_time = current_time
            print(f"📸 Screenshot saved: {screenshot_path}")

        faces = detect_faces_from_frame(frame)
        for face in faces:
            x1, y1, x2, y2 = face["bbox"]
            face_img = frame[y1:y2, x1:x2]
            if face_img.shape[0] == 0 or face_img.shape[1] == 0:
                continue

            face_img_rgb = cv2.cvtColor(face_img, cv2.COLOR_BGR2RGB)
            detected_faces = app.get(face_img_rgb)
            if not detected_faces:
                continue

            face_embedding = detected_faces[0].embedding.flatten()
            label, similarity = recognize_face(face_embedding, students_embeddings, threshold=0.4)  # تقليل الـ threshold
            
            if label != "Unknown":
                detection_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                # إضافة كل اكتشاف مع الوقت والـ session_id
                if label not in detections:
                    detections[label] = []
                detections[label].append({"time": detection_time, "session_id": session_id, "screenshot_path": screenshot_path})
                face_path = f"{known_faces_dir}/face_{session_id}_{label}_{similarity:.2f}_{frame_count}.jpg"
                cv2.imwrite(face_path, cv2.cvtColor(face_img_rgb, cv2.COLOR_RGB2BGR))
                print(f"✅ Detected {label} at {detection_time}, similarity: {similarity:.2f}")
            
            color = (0, 255, 0) if label != "Unknown" else (0, 0, 255)
            cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
            cv2.putText(frame, f"{label} ({similarity:.2f})", (x1, y1 - 10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

        cv2.imshow("Face Recognition", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

def save_final_attendance(lecture_id, detections, late_threshold):
    all_students = students_embeddings.keys()
    
    for student_id in all_students:
        if student_id in detections:
            # أول اكتشاف للطالب
            first_detection = detections[student_id][0]  # أول عنصر في القايمة
            session_id = first_detection["session_id"]
            status = "Present" if session_id == 0 else "Late"
            detection_time = first_detection["time"]
            screenshot_path = first_detection["screenshot_path"]
            detection_count = len(detections[student_id])  # عدد مرات الاكتشاف
        else:
            status = "Absent"
            detection_time = "undetected"
            screenshot_path = None
            detection_count = 0
        
        attendance_record = {
            "lecture_id": lecture_id,
            "student_id": student_id,
            "status": status,
            "detection_time": detection_time,
            "screenshot_path": screenshot_path,
            "detection_count": detection_count  # إضافة عدد الاكتشافات
        }
        
        # حفظ في MongoDB
        result = attendance_collection.update_one(
            {"lecture_id": lecture_id, "student_id": student_id},
            {"$set": attendance_record},
            upsert=True
        )
        print(f"✅ Saved final status for {student_id}: {status}, detected {detection_count} times")
        
        # إرسال للـ Backend
        attendance_record_for_backend = attendance_record.copy()
        attendance_record_for_backend["_id"] = str(result.upserted_id) if result.upserted_id else str(attendance_collection.find_one({"lecture_id": lecture_id, "student_id": student_id})["_id"])
        try:
            requests.post("http://localhost:8080/api/attendances", json=attendance_record_for_backend)
            print(f"✅ Sent final attendance for {student_id} to backend")
        except Exception as e:
            print(f"⚠️ Failed to send to backend: {e}")

def run_camera_for_lecture(lecture_id, lecture_duration, late_threshold, interval, video_path):
    detections = {}  # قاموس لتخزين كل الاكتشافات لكل طالب
    
    print(f"Starting Present session for {late_threshold} seconds...")
    recognize_faces_session(lecture_id, 0, late_threshold, video_path, detections)
    
    remaining_time = lecture_duration - late_threshold
    num_sessions = remaining_time // interval
    print(f"Number of Late sessions: {num_sessions}")
    
    elapsed_time = late_threshold
    for session_id in range(1, num_sessions + 1):
        print(f"Waiting {interval} seconds before session {session_id}...")
        time.sleep(interval)
        elapsed_time += interval
        if elapsed_time < lecture_duration:
            print(f"Starting Late session {session_id}...")
            recognize_faces_session(lecture_id, session_id, interval, video_path, detections)
    
    # حفظ النتيجة النهائية بناءً على أول اكتشاف
    save_final_attendance(lecture_id, detections, late_threshold)

if __name__ == "__main__":
    video_path = "C:/Users/MaysM.M/face-attendance-system/8.mp4"
    run_camera_for_lecture("lecture_1", 300, 60, 30, video_path)