from flask import Flask, request
from main import FaceAttendanceSystem
import requests
import logging

app = Flask(__name__)

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

system = FaceAttendanceSystem()

@app.route('/start', methods=['POST'])
def start_camera():
    data = request.json
    course_id = data.get('course_id')
    lecture_duration = data.get('lecture_duration', 300)
    interval = data.get('interval', 15)
    video_path = "rtsp://admin:maysjoelleshouq@192.168.0.109:554/Streaming/Channels/1"

    if not course_id:
        logger.error("course_id is required")
        return {"error": "course_id is required"}, 400

    try:
        response = requests.get(f"http://localhost:8080/courses/{course_id}")
        if response.status_code == 200:
            course_data = response.json()
            late_threshold = course_data.get('lateThreshold', 300)
        else:
            logger.warning(f"Failed to fetch course {course_id}: {response.status_code}")
            late_threshold = 300
    except requests.exceptions.RequestException as e:
        logger.error(f"Failed to fetch lateThreshold for course {course_id}: {e}")
        late_threshold = 300

    logger.info(f"Starting IP camera with course_id: {course_id}, lecture_duration: {lecture_duration}, "
                f"late_threshold: {late_threshold}, interval: {interval}, video_path: {video_path}")
    system.process_camera(course_id, lecture_duration, late_threshold, interval, video_path)
    return {"status": "IP Camera started", "course_id": course_id}

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)