import os  # للتعامل مع الملفات والمجلدات
import numpy as np  # للتعامل مع العمليات الحسابية والمصفوفات
import cv2  # لمعالجة الصور
import pymongo  # للتعامل مع قاعدة بيانات MongoDB
from insightface.app import FaceAnalysis  # مكتبة التعرف على الوجوه

# إعداد الاتصال بقاعدة بيانات MongoDB
mongo_client = pymongo.MongoClient("mongodb://localhost:27017")  # الاتصال بقاعدة البيانات المحلية
db = mongo_client["face_attendance"]  # اختيار قاعدة البيانات "face_attendance"
students_collection = db["students_embeddings"]  # اختيار مجموعة (Collection) لتخزين التضمينات

# تحميل نموذج التعرف على الوجوه
app = FaceAnalysis()  # إنشاء كائن لتحليل الوجوه
app.prepare(ctx_id=0)  # تهيئة النموذج لاستخدام وحدة المعالجة الرسومية إذا كانت متاحة (استخدم ctx_id=-1 إذا كنت تعمل بدون GPU)

def extract_face_embedding(face_img):
    """
    يستخرج تضمين الوجه من صورة باستخدام ArcFace بعد التأكد من المعالجة الصحيحة للصورة.
    """
    try:
        if face_img is None or face_img.shape[0] == 0 or face_img.shape[1] == 0:
            print("❌ Invalid image input.")  # طباعة رسالة خطأ في حالة كانت الصورة غير صالحة
            return None

        # تحويل الصورة إلى RGB لأن InsightFace يتوقع هذا التنسيق
        face_img = cv2.cvtColor(face_img, cv2.COLOR_BGR2RGB)

        # التأكد من أن حجم الصورة مناسب (112x112 بيكسل) وفقًا لمتطلبات ArcFace
        face_img = cv2.resize(face_img, (112, 112))

        # استخراج الوجه باستخدام نموذج ArcFace
        faces = app.get(face_img)

        # معالجة الأخطاء في حالة عدم العثور على أي وجوه
        if not faces:
            print("❌ No faces detected in `extract_face_embedding` function.")  # طباعة رسالة خطأ عند عدم العثور على أي وجه
            return None
        
        if len(faces) > 1:
            print(f"⚠ Multiple faces detected ({len(faces)}), using the first one.")  # تحذير عند اكتشاف أكثر من وجه، وسيتم استخدام الأول فقط

        print(f"✅ Extracted embedding for face, length: {len(faces[0].embedding)}")  # طباعة عدد القيم في التضمين المستخرج
        return faces[0].embedding  # إرجاع التضمين الخاص بالوجه
    except Exception as e:
        print(f"❌ Error extracting embedding: {e}")  # طباعة رسالة خطأ إذا حدث استثناء أثناء العملية
        return None
