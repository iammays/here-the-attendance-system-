import os  # للتعامل مع الملفات والمجلدات
import numpy as np  # للتعامل مع العمليات الحسابية والمصفوفات
import cv2  # لمعالجة الصور
import pymongo  # للتعامل مع قاعدة بيانات MongoDB
from insightface.app import FaceAnalysis  # مكتبة التعرف على الوجوه

import pathlib
# إعادة توجيه PosixPath إلى WindowsPath لحل مشكلة المسارات على Windows
temp = pathlib.PosixPath
pathlib.PosixPath = pathlib.WindowsPath

# إعداد الاتصال بقاعدة بيانات MongoDB
mongo_client = pymongo.MongoClient("mongodb://localhost:27017")  # الاتصال بقاعدة البيانات المحلية
db = mongo_client["face_attendance"]  # اختيار قاعدة البيانات "face_attendance"
students_collection = db["students_embeddings"]  # اختيار مجموعة (Collection) لتخزين التضمينات

# تحميل نموذج التعرف على الوجوه
app = FaceAnalysis()  # إنشاء كائن لتحليل الوجوه
app.prepare(ctx_id=0)  # تهيئة النموذج لاستخدام وحدة المعالجة الرسومية إذا كانت متاحة

# تحديد مسار مجلد صور الطلاب
dataset_path = "students_faces"  # المسار الذي يحتوي على مجلدات صور كل طالب

# استخراج التضمينات لكل طالب
for student_name in os.listdir(dataset_path):  # التكرار على جميع المجلدات داخل dataset_path
    student_folder = os.path.join(dataset_path, student_name)  # المسار الكامل لمجلد الطالب

    if not os.path.isdir(student_folder):  # التأكد من أن العنصر هو مجلد وليس ملفًا عاديًا
        continue  # تخطي العنصر إذا لم يكن مجلدًا

    embeddings = []  # قائمة لتخزين التضمينات الخاصة بكل صور الطالب
    for img_name in os.listdir(student_folder):  # التكرار على جميع الصور داخل مجلد الطالب
        img_path = os.path.join(student_folder, img_name)  # المسار الكامل للصورة
        img = cv2.imread(img_path)  # قراءة الصورة

        if img is None:  # التحقق مما إذا كانت الصورة غير صالحة
            print(f"⚠ Skipping invalid image: {img_path}")  # طباعة تحذير
            continue  # تخطي الصورة غير الصالحة

        img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)  # تحويل الصورة من BGR إلى RGB لأن InsightFace يستخدم RGB

        # استخراج الوجوه من الصورة
        faces = app.get(img)  # استخدام النموذج لاكتشاف الوجوه
        if len(faces) == 1:  # التأكد من وجود وجه واحد فقط
            embeddings.append(faces[0].embedding.tolist())  # استخراج التضمين وإضافته للقائمة

    if embeddings:  # التحقق من أن هناك تضمينات تم استخراجها
        avg_embedding = np.mean(embeddings, axis=0).tolist()  # حساب متوسط التضمينات لجميع صور الطالب
        students_collection.update_one(
            {"name": student_name},  # البحث عن الطالب باسمه في قاعدة البيانات
            {"$set": {"embedding": avg_embedding}},  # تحديث أو إدراج التضمين الجديد
            upsert=True  # إذا لم يكن الطالب موجودًا، يتم إدخاله تلقائيًا
        )

print("✅ Successfully stored embeddings in MongoDB!")  # طباعة رسالة نجاح عند انتهاء العملية
