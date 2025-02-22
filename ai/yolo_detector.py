import cv2
import numpy as np
import pymongo
from insightface.app import FaceAnalysis
from scipy.spatial.distance import cosine
from pymongo import MongoClient
import os
import time

# إعداد مجلدات حفظ الصور
os.makedirs("screenshots/raw", exist_ok=True)
os.makedirs("screenshots/faces", exist_ok=True)
os.makedirs("screenshots/unknown", exist_ok=True)

# تحميل نموذج ArcFace
app = FaceAnalysis(providers=['CPUExecutionProvider'])
app.prepare(ctx_id=0, det_size=(640, 640))

# الاتصال بقاعدة البيانات
mongo_client = MongoClient("mongodb://localhost:27017")
db = mongo_client["face_attendance"]
students_collection = db["students_embeddings"]

# تحميل التضمينات من قاعدة البيانات
def load_student_embeddings():
    students_embeddings = {}
    students = students_collection.find({}, {"name": 1, "embedding": 1})
    for student in students:
        if "embedding" in student:
            students_embeddings[student["name"]] = np.array(student["embedding"], dtype=np.float32)
    print(f"✅ Loaded {len(students_embeddings)} student embeddings.")
    np.save("students_embeddings_from_mongodb.npy", students_embeddings, allow_pickle=True)
    print("✅ Saved embeddings from MongoDB to students_embeddings_from_mongodb.npy")
    return students_embeddings

students_embeddings = load_student_embeddings()

# التعرف على الوجه
def recognize_face(face_embedding):
    best_match = None
    best_score = 1.0
    for student_name, stored_embedding in students_embeddings.items():
        score = cosine(face_embedding, stored_embedding)
        if score < best_score:
            best_score = score
            best_match = student_name
    if best_match and best_score < 0.5:
        print(f"✅ Recognized: {best_match} (Similarity: {1 - best_score:.2f})")
        return best_match
    else:
        print("❌ Face is unknown.")
        return "Unknown"

# معالجة الفيديو باستخدام ArcFace فقط
def process_video_stream(video_path=0):
    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        print("❌ Failed to open video or camera.")
        return

    last_screenshot_time = time.time()

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        if time.time() - last_screenshot_time >= 1:  # كل ثانية
            timestamp = int(time.time())
            screenshot_path = f"screenshots/raw/frame_{timestamp}.jpg"
            cv2.imwrite(screenshot_path, frame)
            print(f"📸 Saved raw frame at {screenshot_path}")

            # تحويل الفريم إلى RGB وتمريره مباشرة إلى ArcFace
            frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            detected_faces = app.get(frame_rgb)

            if detected_faces:
                print(f"👥 Detected {len(detected_faces)} faces in frame")
                for i, face in enumerate(detected_faces):
                    # استخراج إحداثيات الوجه
                    x1, y1, x2, y2 = face.bbox.astype(int)

                    # التأكد من أن الإحداثيات داخل حدود الفريم
                    x1 = max(0, x1)
                    y1 = max(0, y1)
                    x2 = min(frame.shape[1], x2)
                    y2 = min(frame.shape[0], y2)

                    # استخراج الوجه المقصوص
                    face_crop = frame[y1:y2, x1:x2]

                    # التحقق من أن face_crop ليست فارغة
                    if face_crop.size == 0 or face_crop.shape[0] == 0 or face_crop.shape[1] == 0:
                        print(f"⚠️ Skipping empty face crop for face_{timestamp}_{i}")
                        continue

                    # استخراج التضمين والتعرف
                    face_embedding = face.embedding.flatten()
                    label = recognize_face(face_embedding)

                    # تحديد المسار بناءً على التصنيف
                    if label == "Unknown":
                        face_path = f"screenshots/unknown/face_{timestamp}_{i}.jpg"
                    else:
                        face_path = f"screenshots/faces/face_{timestamp}_{i}.jpg"

                    # حفظ الوجه في المجلد المناسب مع معالجة الاستثناءات
                    try:
                        cv2.imwrite(face_path, face_crop)
                        print(f"🖼️ Saved face at {face_path}")
                    except Exception as e:
                        print(f"❌ Error saving face at {face_path}: {e}")

                    # رسم المستطيل والتسمية على الفريم
                    cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
                    cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
            else:
                print("❌ No faces detected in this frame by ArcFace.")

            last_screenshot_time = time.time()

        cv2.imshow("Video Stream", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

        time.sleep(0.05)

    cap.release()
    cv2.destroyAllWindows()