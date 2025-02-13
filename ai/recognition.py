import os  # للتعامل مع الملفات والمجلدات
import numpy as np  # للتعامل مع العمليات الحسابية والمصفوفات
import cv2  # لمعالجة الصور
import pymongo  # للتعامل مع قاعدة بيانات MongoDB
from insightface.app import FaceAnalysis  # مكتبة التعرف على الوجوه
from scipy.spatial.distance import cosine  # لحساب المسافة بين التضمينات
from pymongo import MongoClient  # لاستيراد عميل MongoDB

# تحميل نموذج ArcFace للتعرف على الوجوه
app = FaceAnalysis()  # إنشاء كائن لتحليل الوجوه
app.prepare(ctx_id=0)  # تهيئة النموذج لاستخدام وحدة المعالجة الرسومية إذا كانت متاحة

# الاتصال بقاعدة البيانات
mongo_client = MongoClient("mongodb://localhost:27017")  # الاتصال بقاعدة البيانات المحلية
db = mongo_client["face_attendance"]  # اختيار قاعدة البيانات "face_attendance"
students_collection = db["students_embeddings"]  # اختيار مجموعة (Collection) لتخزين التضمينات

def load_student_embeddings():
    """
    تحميل التضمينات المخزنة مباشرة من قاعدة البيانات كمصفوفات NumPy.
    """
    students_embeddings = {}  # قاموس لتخزين تضمينات الطلاب
    students = students_collection.find({}, {"name": 1, "embedding": 1})  # استرجاع أسماء الطلاب وتضميناتهم من MongoDB

    for student in students:  # التكرار على جميع الطلاب المخزنين في قاعدة البيانات
        if "embedding" in student:  # التحقق من وجود تضمين لكل طالب
            students_embeddings[student["name"]] = np.array(student["embedding"], dtype=np.float32)  # تحويل التضمين إلى مصفوفة NumPy

    print(f"✅ Loaded {len(students_embeddings)} student embeddings.")  # طباعة عدد التضمينات المحملة
    return students_embeddings

students_embeddings = load_student_embeddings()  # تحميل التضمينات عند بدء التنفيذ

def recognize_faces(image_path):
    """
    يكتشف الوجوه باستخدام ArcFace مباشرةً دون الحاجة إلى YOLO.
    """
    img = cv2.imread(image_path)  # تحميل الصورة
    if img is None:  # التحقق من تحميل الصورة بنجاح
        print("❌ Failed to load image.")  # طباعة رسالة خطأ
        return []  # إرجاع قائمة فارغة

    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)  # تحويل الصورة إلى RGB لأن InsightFace يستخدم هذا التنسيق

    # استخراج الوجوه باستخدام ArcFace
    faces = app.get(img)  # استخدام النموذج لاكتشاف الوجوه
    if not faces:  # التحقق من العثور على وجوه في الصورة
        print("❌ No faces found.")  # طباعة رسالة خطأ
        return []  # إرجاع قائمة فارغة

    recognized_faces = []  # قائمة لتخزين الأسماء المعترف بها

    for i, face in enumerate(faces):  # التكرار على كل الوجوه المكتشفة في الصورة
        face_embedding = face.embedding.flatten()  # استخراج التضمين وتحويله إلى مصفوفة أحادية البعد

        best_match = None  # تخزين أفضل تطابق
        best_score = 1.0  # يبدأ من أعلى قيمة للمسافة

        for student_name, stored_embedding in students_embeddings.items():  # التكرار على جميع تضمينات الطلاب المخزنة
            stored_embedding = np.array(stored_embedding, dtype=np.float32)  # تحويل التضمين إلى مصفوفة NumPy
            score = cosine(face_embedding, stored_embedding)  # حساب المسافة الكونية بين التضمينات

            if score < best_score:  # إذا كان التطابق أفضل من السابق
                best_score = score  # تحديث أفضل درجة تطابق
                best_match = student_name  # تخزين اسم الطالب الذي يطابق الوجه

        # استخدام عتبة مناسبة للتعرف الصحيح
        if best_match and best_score < 0.5:  # التحقق من أن التطابق أقل من العتبة المحددة
            recognized_faces.append(best_match)  # إضافة الاسم إلى القائمة
            print(f"✅ Recognized: {best_match} (Similarity: {1 - best_score:.2f})")  # طباعة النتيجة
        else:
            recognized_faces.append("Unknown")  # إضافة "غير معروف" في حالة عدم التطابق
            print("❌ Face is unknown.")  # طباعة رسالة خطأ

    return recognized_faces  # إرجاع قائمة الأسماء المعترف بها