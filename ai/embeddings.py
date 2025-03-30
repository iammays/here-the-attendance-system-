import os
import numpy as np
import cv2
import pymongo
import rawpy
from insightface.app import FaceAnalysis
import pathlib
from face_utils import ensure_dir

# حل مشكلة المسارات في Windows
pathlib.PosixPath = pathlib.WindowsPath

# إعداد MongoDB
mongo_client = pymongo.MongoClient("mongodb://localhost:27017")
db = mongo_client["face_attendance"]
students_collection = db["students_embeddings"]

# تحميل ArcFace بنفس الإعدادات المستخدمة في main.py
app = FaceAnalysis(allowed_modules=['detection', 'recognition'])
app.prepare(ctx_id=0 if torch.cuda.is_available() else -1, det_size=(320, 320))

def read_image(image_path):
    if not os.path.isfile(image_path):
        print(f"⚠️ {image_path} is not a file, skipping...")
        return None
    if image_path.lower().endswith(".cr2"):
        try:
            with rawpy.imread(image_path) as raw:
                img = raw.postprocess()
                return cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
        except Exception as e:
            print(f"❌ Failed to read CR2 image: {image_path} - {e}")
            return None
    elif image_path.lower().endswith((".jpg", ".jpeg", ".png")):
        return cv2.imread(image_path)
    else:
        print(f"⚠️ Unsupported file format: {image_path}")
        return None

def extract_embeddings_from_folder(base_dir="students_embeddings"):
    if not os.path.exists(base_dir):
        print(f"❌ Folder '{base_dir}' not found")
        return
    
    for student_folder in os.listdir(base_dir):
        student_dir = os.path.join(base_dir, student_folder)
        if not os.path.isdir(student_dir):
            print(f"⚠️ {student_dir} is not a directory, skipping...")
            continue
        
        student_id = student_folder
        embeddings = []
        
        for student_file in os.listdir(student_dir):
            file_path = os.path.join(student_dir, student_file)
            img = read_image(file_path)
            if img is None:
                continue

            faces = app.get(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
            if len(faces) == 1:
                embedding = faces[0].embedding.flatten()
                embeddings.append(embedding)
                print(f"✅ Processed embedding for {student_id} from {file_path}")
            else:
                print(f"❌ No face detected or multiple faces in {file_path}")
        
        if embeddings:
            avg_embedding = np.mean(embeddings, axis=0).tolist()
            students_collection.update_one(
                {"student_id": student_id},
                {"$set": {"embedding": avg_embedding}},
                upsert=True
            )
            print(f"✅ Saved average embedding for student {student_id} (from {len(embeddings)} images)")
        else:
            print(f"❌ No valid embeddings found for student {student_id}")

if __name__ == "__main__":
    folder_path = "students_embeddings"
    extract_embeddings_from_folder(folder_path)