import cv2  # مكتبة معالجة الصور والفيديو
import torch  # مكتبة التعلم العميق

import pathlib
# إعادة توجيه PosixPath إلى WindowsPath لحل مشكلة التحميل في Windows
temp = pathlib.PosixPath
pathlib.PosixPath = pathlib.WindowsPath

# تحميل نموذج YOLO v5 مرة واحدة فقط لاكتشاف الوجوه
model = torch.hub.load("ultralytics/yolov5", "custom", path="C:\\Users\\MaysM.M\\yolov5\\best.pt")

def detect_faces(image_path):
    """
    يكتشف الوجوه في صورة باستخدام YOLO v5.
    """
    img = cv2.imread(image_path)  # تحميل الصورة من المسار المحدد
    
    if img is None:
        print(f"❌ Unable to load image: {image_path}")  # طباعة رسالة خطأ إذا لم يتم تحميل الصورة
        return []

    results = model(img)  # تمرير الصورة إلى النموذج للحصول على التوقعات
    
    faces = []  # قائمة لتخزين المعلومات حول الوجوه المكتشفة
    
    for *xyxy, conf, cls in results.xyxy[0]:  # تحليل النتائج المستخرجة من النموذج
        x1, y1, x2, y2 = map(int, xyxy)  # تحويل إحداثيات الصندوق إلى أعداد صحيحة

        # التأكد من أن الصندوق يحتوي على وجه صالح
        if x2 - x1 > 0 and y2 - y1 > 0:
            faces.append({"bbox": (x1, y1, x2, y2), "confidence": float(conf)})  # حفظ إحداثيات الوجه مع مستوى الثقة
    
    return faces  # إرجاع قائمة تحتوي على الوجوه المكتشفة