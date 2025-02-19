import cv2
import torch
import numpy as np
import pymongo
from insightface.app import FaceAnalysis
from scipy.spatial.distance import cosine
from pymongo import MongoClient
import pathlib

# حل مشكلة مسارات PosixPath في Windows
temp = pathlib.PosixPath
pathlib.PosixPath = pathlib.WindowsPath

# تحميل YOLOv5 مع تفعيل التسريع (GPU) إن أمكن
model = torch.hub.load("ultralytics/yolov5", "custom", path="C:\\Users\\user\\yolov5\\best.pt", force_reload=True)
model.conf = 0.5  # تعديل العتبة لزيادة الدقة
model.iou = 0.45  # تحسين اكتشاف الأجسام المتداخلة

# تحميل نموذج ArcFace
app = FaceAnalysis(allowed_modules=['detection', 'recognition'])
app.prepare(ctx_id=0)

# الاتصال بقاعدة البيانات
mongo_client = MongoClient("mongodb://localhost:27017")
db = mongo_client["face_attendance"]
students_collection = db["students_embeddings"]

def load_student_embeddings():
    students_embeddings = {}
    students = students_collection.find({}, {"name": 1, "embedding": 1})

    for student in students:
        if "embedding" in student:
            students_embeddings[student["name"]] = np.array(student["embedding"], dtype=np.float32)

    print(f"✅ Loaded {len(students_embeddings)} student embeddings.")
    return students_embeddings

students_embeddings = load_student_embeddings()

def detect_faces_from_frame(frame):
    """
    يكتشف الوجوه من فريم الفيديو باستخدام YOLO v5.
    """
    frame_resized = cv2.resize(frame, (640, 480))  # تقليل الدقة لتحسين السرعة
    results = model(frame_resized)

    faces = []
    for *xyxy, conf, cls in results.xyxy[0]:  
        x1, y1, x2, y2 = map(int, xyxy)
        x1, y1, x2, y2 = int(x1 * frame.shape[1] / 640), int(y1 * frame.shape[0] / 480), \
                         int(x2 * frame.shape[1] / 640), int(y2 * frame.shape[0] / 480)
        
        if x2 - x1 > 0 and y2 - y1 > 0:
            faces.append({"bbox": (x1, y1, x2, y2), "confidence": float(conf)})

    return faces

def recognize_faces_from_video(video_path):
    """
    يفتح فيديو ويتعرف على الوجوه فريمًا فريمًا باستخدام ArcFace.
    """
    cap = cv2.VideoCapture(video_path)

    if not cap.isOpened():
        print("❌ Failed to open video.")
        return

    frame_count = 0

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        frame_count += 1
        if frame_count % 3 != 0:  # تحليل كل 3 فريمات فقط
            continue

        faces = detect_faces_from_frame(frame)

        for face in faces:
            x1, y1, x2, y2 = face["bbox"]
            face_img = frame[y1:y2, x1:x2]

            if face_img.shape[0] == 0 or face_img.shape[1] == 0:
                continue

            face_img = cv2.resize(face_img, (112, 112))  # تقليل حجم الوجه قبل التحليل
            face_img = cv2.cvtColor(face_img, cv2.COLOR_BGR2RGB)
            detected_faces = app.get(face_img)

            if not detected_faces:
                continue

            face_embedding = detected_faces[0].embedding.flatten()
            best_match = None
            best_score = 1.0

            for student_name, stored_embedding in students_embeddings.items():
                score = cosine(face_embedding, stored_embedding)

                if score < best_score:
                    best_score = score
                    best_match = student_name

            label = best_match if best_match and best_score < 0.5 else "Unknown"

            color = (0, 255, 0) if label != "Unknown" else (0, 0, 255)
            cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
            cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

        cv2.imshow("Face Recognition", frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()
