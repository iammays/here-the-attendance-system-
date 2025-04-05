# ai/app.py
from flask import Flask, request
from main import run_camera_for_lecture

app = Flask(__name__)

@app.route('/start', methods=['POST'])
def start_camera():
    data = request.json
    lecture_id = data.get('lecture_id', 'default_lecture')
    lecture_duration = data.get('lecture_duration', 300)  # 5 دقايق
    late_threshold = data.get('late_threshold', 10)      # 10 ثواني
    interval = data.get('interval', 15)                  # 15 ثانية
    video_path = data.get('video_path', "C:/Users/MaysM.M/face-attendance-system/8.mp4")

    run_camera_for_lecture(lecture_id, lecture_duration, late_threshold, interval, video_path)
    return {"status": "Camera started", "lecture_id": lecture_id}

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)