#C:\Users\MaysM.M\face-attendance-system\ai\test_ai.py

from recognition import recognize_faces

image_path = "8.jpg"  
results = recognize_faces(image_path)

for name in results:
    print(f"Detected: {name}")

