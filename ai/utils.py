import numpy as np
from pymongo import MongoClient

# الاتصال بقاعدة البيانات
mongo_client = MongoClient("mongodb://localhost:27017")
db = mongo_client["face_attendance"]
students_collection = db["students_embeddings"]

def load_student_embeddings():
    """
    تحميل التضمينات (embeddings) الخاصة بالطلاب من قاعدة البيانات.
    """
    students_embeddings = {}
    students = students_collection.find({}, {"name": 1, "embedding": 1})
    for student in students:
        if "embedding" in student:
            students_embeddings[student["name"]] = np.array(student["embedding"], dtype=np.float32)
    print(f"✅ Loaded {len(students_embeddings)} student embeddings.")
    return students_embeddings