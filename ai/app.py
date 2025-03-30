from flask import Flask, request
from main import run_camera_for_lecture

app = Flask(__name__)

@app.route('/start', methods=['POST'])
def start_camera():
    data = request.json
    lecture_id = data['lecture_id']
    lecture_duration = data['lecture_duration']
    late_threshold = data['late_threshold']
    interval = data['interval']
    video_path = data.get('video_path', "C:/Users/MaysM.M/face-attendance-system/8.mp4")
    run_camera_for_lecture(lecture_id, lecture_duration, late_threshold, interval, video_path)
    return {"status": "Camera started"}

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)