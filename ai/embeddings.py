import os
import numpy as np
import cv2
import pymongo
import rawpy
from insightface.app import FaceAnalysis
import pathlib

# حل مشكلة مسارات PosixPath في Windows
pathlib.PosixPath = pathlib.WindowsPath

# إعداد الاتصال بقاعدة بيانات MongoDB
mongo_client = pymongo.MongoClient("mongodb://localhost:27017")
db = mongo_client["face_attendance"]
students_collection = db["students_embeddings"]

# تحميل نموذج ArcFace
app = FaceAnalysis(providers=['CPUExecutionProvider'])
app.prepare(ctx_id=0, det_size=(640, 640))

# تحديد مسار مجلد صور الطلاب
dataset_path = "students_faces"

# قراءة الصور سواء كانت JPG أو CR2
def read_image(image_path):
    """ قراءة الصورة مع دعم لصيغة CR2 """
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

# استخراج التضمينات لكل طالب
for student_name in os.listdir(dataset_path):
    student_folder = os.path.join(dataset_path, student_name)
    if not os.path.isdir(student_folder):
        continue

    embeddings = []
    for img_name in os.listdir(student_folder):
        img_path = os.path.join(student_folder, img_name)
        img = read_image(img_path)

        if img is None:
            print(f"⚠️ Skipping invalid image: {img_path}")
            continue

        # تمرير الصورة مباشرة إلى ArcFace
        faces = app.get(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
        if len(faces) == 1:
            embeddings.append(faces[0].embedding.flatten())
        else:
            print(f"❌ No face detected in image: {img_path}")

    if embeddings:
        avg_embedding = np.mean(embeddings, axis=0).tolist()
        result = students_collection.update_one(
            {"name": student_name},
            {"$set": {"embedding": avg_embedding}},
            upsert=True
        )
        if result.matched_count > 0:
            print(f"✅ Updated embeddings for {student_name}")
        else:
            print(f"✅ Inserted new embeddings for {student_name}")
    else:
        print(f"⚠️ No valid face found for {student_name}")