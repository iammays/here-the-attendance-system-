
#ai\main.py 

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

# إعداد المسار المحلي لـ YOLOv5 واستيراد الدوال المساعدة
sys.path.append("C:\\Users\\MaysM.M\\yolov5")
from utils.general import scale_boxes
from face_utils import load_student_embeddings, recognize_face, ensure_dir

# حل مشكلة توافق المسارات في Windows
temp = pathlib.PosixPath
pathlib.PosixPath = pathlib.WindowsPath

# تحميل نموذج YOLOv5 من المسار المحلي وضبط إعداداته
model = torch.hub.load("C:/Users/MaysM.M/yolov5", "custom", path="C:\\Users\\MaysM.M\\yolov5\\best.pt", source="local", force_reload=True)
model.conf = 0.6  
model.iou = 0.1  # للتداخل 

# تحميل نموذج ArcFace وضبطه
app = FaceAnalysis(allowed_modules=['detection', 'recognition'])
app.prepare(ctx_id=0, det_size=(320, 320))  # تقليل det_size للصور المقصوصة

# الاتصال بقاعدة بيانات MongoDB وتحميل بيانات الطلاب
mongo_client = MongoClient("mongodb://localhost:27017")
db = mongo_client["face_attendance"]
students_collection = db["students_embeddings"]
attendance_collection = db["attendance"]  # مجموعة جديدة للحضور
students_embeddings = load_student_embeddings(students_collection)

# إعداد المجلدات لتخزين الصور
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
    """Detect faces from a frame using YOLOv5 with manual scaling"""
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
            face_location = {
                "bbox": (x1, y1, x2, y2),
                "confidence": float(conf),
                "center": (int((x1 + x2) / 2), int((y1 + y2) / 2)),
                "width": x2 - x1,
                "height": y2 - y1
            }
            faces.append(face_location)
            print(f"[DEBUG] Face detected: bbox=({x1},{y1},{x2},{y2}), "
                  f"center=({face_location['center']}), confidence={conf:.2f}")
    
    return faces

def recognize_faces_from_video():
    rtsp_url = "rtsp://admin:maysjoelleshouq@192.168.0.116:554/live"
    cap = cv2.VideoCapture(rtsp_url)
    cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
    cap.set(cv2.CAP_PROP_FPS, 15)
    
    if not cap.isOpened():
        print("❌ Failed to connect to IP camera. Check RTSP URL or network.")
        return
    
    frame_count = 0
    face_count = 0
    recognized_persons = {}
    total_faces_detected = 0
    process_interval = 1

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret or frame is None or frame.size == 0:
            print("❌ Failed to retrieve frame or empty frame. Reconnecting...")
            cap.release()
            cap = cv2.VideoCapture(rtsp_url)
            cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
            cap.set(cv2.CAP_PROP_FPS, 15)
            continue
        
        frame = cv2.resize(frame, (640, 480))
        frame_count += 1
        start_time = time.time()

        screenshot_path = f"{screenshots_dir}/frame_{frame_count}.jpg"
        cv2.imwrite(screenshot_path, frame)
        print(f"📸 Saved screenshot: {screenshot_path}")

        faces = detect_faces_from_frame(frame)
        if not faces:
            print("[DEBUG] No faces detected in this frame.")

        for face in faces:
            x1, y1, x2, y2 = face["bbox"]
            face_img = frame[y1:y2, x1:x2]
            if face_img.shape[0] == 0 or face_img.shape[1] == 0:
                continue

            yolo_face_path = f"{yolo_cropped_dir}/yolo_face_{frame_count}_{face_count}.jpg"
            cv2.imwrite(yolo_face_path, face_img)

            face_img_rgb = cv2.cvtColor(face_img, cv2.COLOR_BGR2RGB)
            detected_faces = app.get(face_img_rgb)
            if not detected_faces:
                total_faces_detected += 1
                continue

            total_faces_detected += 1
            face_embedding = detected_faces[0].embedding.flatten()
            label, similarity = recognize_face(face_embedding, students_embeddings)
            face_count += 1
            
            # تسجيل الوقت
            detection_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            if label != "Unknown":
                if label not in recognized_persons:
                    recognized_persons[label] = []
                recognized_persons[label].append(detection_time)
                face_path = f"{known_faces_dir}/face_{frame_count}_{label}_{similarity:.2f}.jpg"
                print(f"✅ Recognized {label} at {detection_time}, saved to: {face_path}")
                
                # حفظ في MongoDB
                attendance_record = {
                    "student_name": label,
                    "detection_time": detection_time,
                    "similarity": float(similarity),
                    "status": "Present",  # حالة مؤقتة، بتتعدل في Spring Boot
                    "frame_count": frame_count,
                    "screenshot_path": screenshot_path
                }
                attendance_collection.insert_one(attendance_record)
            else:
                face_path = f"{unknown_faces_dir}/face_{frame_count}_unknown.jpg"
                print(f"❌ Unknown face at {detection_time}, saved to: {face_path}")

            cv2.imwrite(face_path, cv2.cvtColor(face_img_rgb, cv2.COLOR_RGB2BGR))

            color = (0, 255, 0) if label != "Unknown" else (0, 0, 255)
            cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
            cv2.putText(frame, f"{label} ({similarity:.2f})", (x1, y1 - 10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

        cv2.imshow("Face Recognition", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

        elapsed_time = time.time() - start_time
        if elapsed_time < process_interval:
            time.sleep(process_interval - elapsed_time)

    print("\n=== Summary ===")
    print(f"Total faces detected: {total_faces_detected}")
    print("Recognized persons and times:")
    for person, times in recognized_persons.items():
        print(f" - {person}: detected {len(times)} time(s) at {', '.join(times)}")
    print(f"Unknown faces: {total_faces_detected - sum(len(times) for times in recognized_persons.values())}")

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    recognize_faces_from_video()