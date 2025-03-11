import os
import numpy as np
import cv2
import pymongo
import rawpy
from insightface.app import FaceAnalysis
import pathlib
import zipfile

# حل مشكلة المسارات في Windows
pathlib.PosixPath = pathlib.WindowsPath

# إعداد MongoDB
mongo_client = pymongo.MongoClient("mongodb://localhost:27017")
db = mongo_client["face_attendance"]
students_collection = db["students_embeddings"]

# تحميل ArcFace
app = FaceAnalysis(providers=['CPUExecutionProvider'])
app.prepare(ctx_id=0, det_size=(640, 640))

# قراءة الصورة (JPG أو CR2)
def read_image(image_path, temp_dir=None):
    if image_path.lower().endswith(".cr2"):
        try:
            with rawpy.imread(image_path) as raw:
                img = raw.postprocess()
                return cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
        except Exception as e:
            print(f"❌ Failed to read CR2 image: {image_path} - {e}")
            return None
    else:
        return cv2.imread(image_path)

# استخراج التضمينات من ملف مضغوط
def extract_embeddings_from_zip(zip_path, temp_dir="temp_extracted"):
    ensure_dir(temp_dir)
    with zipfile.ZipFile(zip_path, 'r') as zip_ref:
        zip_ref.extractall(temp_dir)
    
    for student_file in os.listdir(temp_dir):
        student_id = os.path.splitext(student_file)[0]  # رقم الطالب من اسم الملف
        file_path = os.path.join(temp_dir, student_file)
        
        img = read_image(file_path)
        if img is None:
            print(f"⚠️ Skipping invalid image: {file_path}")
            continue

        faces = app.get(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
        if len(faces) == 1:
            embedding = faces[0].embedding.flatten().tolist()
            students_collection.update_one(
                {"name": student_id},  # رقم الطالب كـ "name"
                {"$set": {"embedding": embedding}},
                upsert=True
            )
            print(f"✅ Saved embedding for student {student_id}")
        else:
            print(f"❌ No face detected in {file_path}")

# التأكد من وجود المجلد
def ensure_dir(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)

if __name__ == "__main__":
    zip_path = "students_embeddings.zip"  # مسار الملف المضغوط
    extract_embeddings_from_zip(zip_path)