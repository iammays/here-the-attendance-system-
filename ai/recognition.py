import cv2
import numpy as np
from scipy.spatial.distance import cosine
from insightface.app import FaceAnalysis
from ai.utils import load_student_embeddings

# تحميل نموذج ArcFace
app = FaceAnalysis(providers=['CPUExecutionProvider'])
app.prepare(ctx_id=0, det_size=(640, 640))

# تحميل التضمينات
students_embeddings = load_student_embeddings()

def recognize_faces_from_video(video_path):
    """
    يفتح فيديو ويتعرف على الوجوه فريمًا فريمًا باستخدام ArcFace.
    """
    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        print("❌ Failed to open video.")
        return

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        img_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        faces = app.get(img_rgb)

        for face in faces:
            face_embedding = face.embedding.flatten()
            best_match = None
            best_score = 1.0
            for student_name, stored_embedding in students_embeddings.items():
                score = cosine(face_embedding, stored_embedding)
                if score < best_score:
                    best_score = score
                    best_match = student_name

            if best_match and best_score < 0.5:
                label = best_match
                print(f"✅ Recognized: {best_match} (Similarity: {1 - best_score:.2f})")
            else:
                label = "Unknown"
                print("❌ Face is unknown.")

            x1, y1, x2, y2 = face.bbox.astype(int)
            cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
            cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

        cv2.imshow("Face Recognition", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

