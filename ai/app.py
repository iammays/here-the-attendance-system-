from flask import Flask, request
from main import run_camera_for_lecture
import requests

app = Flask(__name__)

@app.route('/start', methods=['POST'])
def start_camera():
    data = request.json
    course_id = data.get('course_id')
    lecture_duration = data.get('lecture_duration', 300)
    interval = data.get('interval', 15)
    video_path = data.get('video_path', "C:/Users/MaysM.M/face-attendance-system/8.mp4")

    if not course_id:
        return {"error": "course_id is required"}, 400

    # جلب lateThreshold من الـ backend
    try:
        response = requests.get(f"http://localhost:8080/courses/{course_id}")
        if response.status_code == 200:
            course_data = response.json()
            late_threshold = course_data.get('lateThreshold', 10)  # افتراضي 10 ثوانٍ
        else:
            print(f"⚠️ Failed to fetch course {course_id}: {response.status_code}")
            late_threshold = 10
    except requests.exceptions.RequestException as e:
        print(f"⚠️ Failed to fetch lateThreshold for course {course_id}: {e}")
        late_threshold = 10

    print(f"[DEBUG] Starting camera with course_id: {course_id}, lecture_duration: {lecture_duration}, late_threshold: {late_threshold}, interval: {interval}, video_path: {video_path}")
    run_camera_for_lecture(course_id, lecture_duration, late_threshold, interval, video_path)
    return {"status": "Camera started", "course_id": course_id}

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)