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
import json
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
known_faces_dir = f"{output_dir}/known_faces"
unknown_faces_dir = f"{output_dir}/unknown_faces"
yolo_cropped_dir = f"{output_dir}/yolo_cropped_faces"
ensure_dir(screenshots_dir)
ensure_dir(known_faces_dir)
ensure_dir(unknown_faces_dir)
ensure_dir(yolo_cropped_dir)

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
        
        frame = cv2.resize(frame, (1280, 720))
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
            padding = int(max(x2 - x1, y2 - y1) * 0.5)
            x1 = max(0, x1 - padding)
            y1 = max(0, y1 - padding)
            x2 = min(frame.shape[1], x2 + padding)
            y2 = min(frame.shape[0], y2 + padding)
            face_img = frame[y1:y2, x1:x2]
            print(f"[DEBUG] Cropped face size: {face_img.shape}")
            if face_img.shape[0] < 40 or face_img.shape[1] < 40:
                print(f"⚠️ Face too small at {x1},{y1},{x2},{y2}")
                continue

            face_img_rgb = cv2.cvtColor(face_img, cv2.COLOR_BGR2RGB)
            detected_faces = app.get(face_img_rgb)
            if not detected_faces:
                print(f"⚠️ ArcFace failed to detect face at {x1},{y1},{x2},{y2}")
                continue

            face_embedding = detected_faces[0].embedding.flatten()
            label, similarity = recognize_face(face_embedding, students_embeddings, threshold=0.6)
            print(f"[DEBUG] Face at {x1},{y1},{x2},{y2} - Label: {label}, Similarity: {similarity:.2f}")
            if label != "Unknown":
                detection_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
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
            first_detection = detections[student_id][0]
            session_id = first_detection["session_id"]
            status = "Present" if session_id == 0 else "Late"
            detection_time = first_detection["time"]
            screenshot_path = first_detection["screenshot_path"]
            detection_count = len(detections[student_id])
        else:
            status = "Absent"
            detection_time = "undetected"
            screenshot_path = None
            detection_count = 0
            session_id = 0
        
        attendance_record = {
            "lecture_id": lecture_id,
            "student_id": student_id,
            "status": status,
            "detection_time": detection_time,
            "screenshot_path": screenshot_path,
            "detection_count": detection_count
        }
        
        result = attendance_collection.update_one(
            {"lecture_id": lecture_id, "student_id": student_id},
            {"$set": attendance_record},
            upsert=True
        )
        print(f"✅ Saved final status for {student_id}: {status}, detected {detection_count} times in MongoDB")
        
        attendance_record_for_backend = {
            "lectureId": lecture_id,
            "sessionId": session_id,
            "studentId": student_id,
            "detectionTime": detection_time,
            "screenshotPath": screenshot_path,
            "status": status
        }
        
        print(f"[DEBUG] Sending to backend: {json.dumps(attendance_record_for_backend, indent=2)}")
        try:
            response = requests.post("http://localhost:8080/api/attendances", json=attendance_record_for_backend)
            response.raise_for_status()
            print(f"✅ Sent final attendance for {student_id} to backend - Response: {response.status_code}")
        except requests.exceptions.RequestException as e:
            print(f"⚠️ Failed to send to backend for {student_id}: {e}")

def run_camera_for_lecture(lecture_id, lecture_duration, late_threshold, interval, video_path):
    detections = {}
    cap = cv2.VideoCapture(video_path)  # نفتح الفيديو مرة واحدة فقط
    if not cap.isOpened():
        print(f"❌ Failed to open video: {video_path}")
        return
    
    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))  # عدد الإطارات الكلي
    fps = cap.get(cv2.CAP_PROP_FPS)  # معدل الإطارات في الثانية
    print(f"[DEBUG] Video total frames: {total_frames}, FPS: {fps}")

    def process_session(session_id, duration, start_frame):
        cap.set(cv2.CAP_PROP_POS_FRAMES, start_frame)  # نحدد بداية السيشن
        start_time = time.time()
        frame_count = 0
        last_screenshot_time = 0
        screenshot_interval = 1
        
        while cap.isOpened() and (time.time() - start_time) < duration:
            ret, frame = cap.read()
            if not ret or frame is None:
                print(f"❌ End of video reached in session {session_id}")
                break
            
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
                label, similarity = recognize_face(face_embedding, students_embeddings, threshold=0.6)
                if label != "Unknown":
                    detection_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                    if label not in detections:
                        detections[label] = []
                    detections[label].append({"time": detection_time, "session_id": session_id, "screenshot_path": screenshot_path})
                    print(f"✅ Detected {label} at {detection_time}, similarity: {similarity:.2f}")

            cv2.imshow("Face Recognition", frame)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
        
        current_frame = int(cap.get(cv2.CAP_PROP_POS_FRAMES))  # نرجع آخر إطار وصلناله
        return current_frame

    # سيشن Present
    print(f"Starting Present session for {late_threshold} seconds...")
    current_frame = process_session(0, late_threshold, 0)

    # سيشنات Late
    remaining_time = lecture_duration - late_threshold
    num_sessions = remaining_time // 60
    print(f"Number of Late sessions: {num_sessions}")
    
    elapsed_time = late_threshold
    for session_id in range(1, num_sessions + 1):
        print(f"Waiting 10 seconds before session {session_id}...")
        time.sleep(10)
        elapsed_time += 60
        if elapsed_time < lecture_duration and current_frame < total_frames:
            print(f"Starting Late session {session_id}...")
            current_frame = process_session(session_id, 60, current_frame)
        else:
            print(f"[DEBUG] Lecture duration ({lecture_duration}s) or end of video reached, stopping sessions.")
            break
    
    cap.release()
    cv2.destroyAllWindows()
    save_final_attendance(lecture_id, detections, late_threshold)
    
if __name__ == "__main__":
    video_path = "C:/Users/MaysM.M/face-attendance-system/8.mp4"
    run_camera_for_lecture("lecture_1", 300, 60, 10, video_path)