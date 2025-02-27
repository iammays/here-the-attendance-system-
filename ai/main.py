# main.py

import cv2
import torch
import numpy as np
import pymongo
from insightface.app import FaceAnalysis
from pymongo import MongoClient
import pathlib
import sys

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
    original_shape = frame.shape  # (height, width, channels)
    frame_resized = cv2.resize(frame, (640, 480))
    results = model(frame_resized)
    faces = []
    print(f"[DEBUG] Number of objects detected by YOLO: {len(results.xyxy[0])}")
    
    for *xyxy, conf, cls in results.xyxy[0]:
        x1, y1, x2, y2 = map(int, xyxy)
        # تحجيم يدوي للإحداثيات من 640x480 إلى الحجم الأصلي
        x1 = int(x1 * original_shape[1] / 640)
        y1 = int(y1 * original_shape[0] / 480)
        x2 = int(x2 * original_shape[1] / 640)
        y2 = int(y2 * original_shape[0] / 480)
        # التأكد من أن الإحداثيات داخل حدود الصورة
        x1, y1 = max(0, x1), max(0, y1)
        x2, y2 = min(original_shape[1], x2), min(original_shape[0], y2)
        
        if x2 - x1 > 0 and y2 - y1 > 0:
            face_location = {
                "bbox": (x1, y1, x2, y2),           # Face coordinates in original frame
                "confidence": float(conf),          # Detection confidence
                "center": (int((x1 + x2) / 2), int((y1 + y2) / 2)),  # Center of face
                "width": x2 - x1,                   # Width of face bounding box
                "height": y2 - y1                   # Height of face bounding box
            }
            faces.append(face_location)
            print(f"[DEBUG] Face detected: bbox=({x1},{y1},{x2},{y2}), "
                  f"center=({face_location['center']}), confidence={conf:.2f}")
    
    return faces

# التعرف على الوجوه في الفيديو باستخدام YOLO وArcFace
def recognize_faces_from_video(video_path):
    """Recognize faces from a video using YOLO and ArcFace"""
    cap = cv2.VideoCapture(video_path)  # استخدام الكاميرا بدلاً من ملف فيديو
    if not cap.isOpened():
        print("❌ Failed to open video.")
        return
    fps = cap.get(cv2.CAP_PROP_FPS)
    frame_interval = int(fps)
    frame_count = 0
    face_count = 0
    recognized_persons = {}  
    total_faces_detected = 0  
    print("frame_interval   ", frame_interval)

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break
        frame_count += 1
        if frame_count % frame_interval != 0:
            continue

        # حفظ لقطة الفريم
        screenshot_path = f"{screenshots_dir}/frame_{frame_count}.jpg"
        cv2.imwrite(screenshot_path, frame)
        print(f"📸 Saved screenshot: {screenshot_path}")

        # اكتشاف الوجوه
        faces = detect_faces_from_frame(frame)
        if not faces:
            print("[DEBUG] No faces detected in this frame.")

        # معالجة كل وجه مكتشف
        for face in faces:
            x1, y1, x2, y2 = face["bbox"]
            face_img = frame[y1:y2, x1:x2]
            if face_img.shape[0] == 0 or face_img.shape[1] == 0:
                print("[DEBUG] Cropped face image is empty, skipping.")
                continue

            # حفظ الوجه المقصوص من YOLO
            yolo_face_path = f"{yolo_cropped_dir}/yolo_face_{frame_count}_{face_count}.jpg"
            cv2.imwrite(yolo_face_path, face_img)
            print(f"[DEBUG] YOLO cropped face saved to: {yolo_face_path}")

            # تحويل الصورة وتمريرها إلى ArcFace
            face_img_rgb = cv2.cvtColor(face_img, cv2.COLOR_BGR2RGB)
            detected_faces = app.get(face_img_rgb)
            if not detected_faces:
                print("[DEBUG] ArcFace did not detect a face in the cropped image.")
                total_faces_detected += 1
                continue

            # التعرف على الوجه
            total_faces_detected += 1
            face_embedding = detected_faces[0].embedding.flatten()
            label, similarity = recognize_face(face_embedding, students_embeddings)
            face_count += 1
            if label != "Unknown":
                recognized_persons[label] = recognized_persons.get(label, 0) + 1
                face_path = f"{known_faces_dir}/face_{face_count}_{label}_{similarity:.2f}.jpg"
                print(f"✅ Recognized {label}, saved to: {face_path}")
            else:
                face_path = f"{unknown_faces_dir}/face_{face_count}_unknown.jpg"
                print(f"❌ Unknown face, saved to: {face_path}")
            cv2.imwrite(face_path, cv2.cvtColor(face_img_rgb, cv2.COLOR_RGB2BGR))

            # رسم المربع والتسمية على الفريم
            color = (0, 255, 0) if label != "Unknown" else (0, 0, 255)
            cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
            cv2.putText(frame, f"{label} ({similarity:.2f})", (x1, y1 - 10), 
                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

        cv2.imshow("Face Recognition", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    print("\n=== Summary of Face Recognition Results ===")
    print(f"Total faces detected: {total_faces_detected}")
    print("Recognized persons:")
    if recognized_persons:
        for person, count in recognized_persons.items():
            print(f" - {person}: {count} time(s)")
    else:
        print(" - No recognized persons.")
    print(f"Unknown faces: {total_faces_detected - sum(recognized_persons.values())}")

    cap.release()
    cv2.destroyAllWindows()

