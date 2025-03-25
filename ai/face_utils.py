import numpy as np
from scipy.spatial.distance import cosine
import os

def load_student_embeddings(students_collection):
    students_embeddings = {}
    students = students_collection.find({}, {"student_id": 1, "embedding": 1})
    for student in students:
        if "embedding" in student:
            students_embeddings[student["student_id"]] = np.array(student["embedding"], dtype=np.float32)
    print(f"✅ Loaded {len(students_embeddings)} student embeddings.")
    return students_embeddings

def recognize_face(face_embedding, students_embeddings, threshold=0.4):
    best_match = None
    best_score = 1.0
    for student_id, stored_embedding in students_embeddings.items():
        score = cosine(face_embedding, stored_embedding)
        if score < best_score:
            best_score = score
            best_match = student_id
    print(f"[DEBUG] Best match: {best_match}, Best score: {best_score:.2f}")
    if best_match and best_score < threshold:
        return best_match, 1 - best_score
    return "Unknown", 0

def ensure_dir(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)