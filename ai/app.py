from flask import Flask, request
from main import run_camera_for_lecture

app = Flask(__name__)

@app.route('/start', methods=['POST'])
def start_camera():
    data = request.json
    run_camera_for_lecture(data['lecture_id'], data['lecture_duration'], data['late_threshold'], data['interval'])
    return {"status": "Camera started"}

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)