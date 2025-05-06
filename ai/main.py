import cv2
import torch
import sys
import time
import schedule
import requests
import logging
from datetime import datetime
from pymongo import MongoClient
from insightface.app import FaceAnalysis
from face_utils import load_student_embeddings, recognize_face, ensure_dir

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class FaceAttendanceSystem:
    def __init__(self):
        self.mongo_client = MongoClient("mongodb://localhost:27017")
        self.face_db = self.mongo_client["face_attendance"]
        self.backend_db = self.mongo_client["backend_db"]
        self.students_collection = self.face_db["students_embeddings"]
        self.attendance_collection = self.face_db["attendance"]
        self.courses_collection = self.backend_db["courses"]

        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        logger.info(f"Using device: {self.device}")

        sys.path.append("C:/Users/MaysM.M/yolov5")
        self.model = torch.hub.load("C:/Users/MaysM.M/yolov5", "custom", path="C:\\Users\\MaysM.M\\yolov5\\best.pt", source="local", force_reload=True)
        self.model.conf = 0.6
        self.model.iou = 0.1
        self.model.to(self.device)
        logger.info("YOLOv5 model loaded successfully")

        self.app = FaceAnalysis(allowed_modules=['detection', 'recognition'])
        self.app.prepare(ctx_id=0 if torch.cuda.is_available() else -1, det_size=(640, 640))
        logger.info("InsightFace model loaded successfully")

        self.output_dir = "output"
        self.screenshots_dir = f"{self.output_dir}/screenshots"
        self.known_faces_dir = f"{self.output_dir}/known_faces"
        self.unknown_faces_dir = f"{self.output_dir}/unknown_faces"
        self.yolo_cropped_dir = f"{self.output_dir}/yolo_cropped_faces"

        ensure_dir(self.screenshots_dir)
        ensure_dir(self.known_faces_dir)
        ensure_dir(self.unknown_faces_dir)
        ensure_dir(self.yolo_cropped_dir)

        self.students_embeddings = self.load_embeddings()

    def load_embeddings(self):
        students_embeddings = load_student_embeddings(self.students_collection)
        logger.info(f"Loaded {len(students_embeddings)} student embeddings")
        return students_embeddings

    def detect_faces_from_frame(self, frame):
        results = self.model(frame)
        faces = []
        logger.debug(f"Number of objects detected by YOLO: {len(results.xyxy[0])}")

        for *xyxy, conf, cls in results.xyxy[0]:
            x1, y1, x2, y2 = map(int, xyxy)
            x1, y1 = max(0, x1), max(0, y1)
            x2, y2 = min(frame.shape[1], x2), min(frame.shape[0], y2)

            if x2 - x1 > 0 and y2 - y1 > 0:
                face_location = {"bbox": (x1, y1, x2, y2), "confidence": float(conf)}
                faces.append(face_location)
        return faces

    def calculate_sessions(self, lecture_duration, late_threshold, interval):
        remaining_time = lecture_duration - late_threshold
        num_sessions = max(1, remaining_time // (interval + 1)) + 1
        return num_sessions

    def process_camera(self, course_id, lecture_duration, late_threshold, interval, video_path):
        cap = cv2.VideoCapture(video_path)
        cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
        if not cap.isOpened():
            logger.error(f"Could not open RTSP stream at {video_path}")
            return

        lecture_id = f"{course_id}-{datetime.now().strftime('%Y%m%d-%H%M')}"
        detections = {}
        first_detected_at = {}
        total_sessions = self.calculate_sessions(lecture_duration, late_threshold, interval)
        session_times = [0]
        for i in range(1, total_sessions):
            if i == 1:
                session_times.append(late_threshold)
            else:
                session_times.append(session_times[i-1] + interval + 1)

        logger.info(f"Starting IP camera with course_id: {course_id}, lecture_duration: {lecture_duration}, "
                    f"late_threshold: {late_threshold}, interval: {interval}, total_sessions: {total_sessions}")

        start_time = time.time()
        current_session = 0
        frame_count = 0

        while True:
            ret, frame = cap.read()
            if not ret or frame is None or frame.size == 0:
                logger.warning("Failed to retrieve frame. Reconnecting...")
                cap.release()
                cap = cv2.VideoCapture(video_path)
                cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
                if not cap.isOpened():
                    logger.error("Failed to reconnect to RTSP. Exiting...")
                    break
                continue

            frame_count += 1
            elapsed_time = time.time() - start_time
            if elapsed_time >= lecture_duration:
                logger.info(f"Lecture duration ({lecture_duration} seconds) completed")
                break

            while current_session < total_sessions - 1 and elapsed_time >= session_times[current_session + 1]:
                current_session += 1
                logger.debug(f"Switching to session {current_session} at {elapsed_time:.2f}s")

            if current_session not in detections:
                detections[current_session] = []

            faces = self.detect_faces_from_frame(frame)
            current_time_str = datetime.now().strftime("%H:%M:%S")
            screenshot_path = f"{self.screenshots_dir}/frame_{lecture_id}_{frame_count}.jpg"
            cv2.imwrite(screenshot_path, frame)

            for face in faces:
                x1, y1, x2, y2 = face["bbox"]
                padding = int(max(x2 - x1, y2 - y1) * 0.5)
                x1 = max(0, x1 - padding)
                y1 = max(0, y1 - padding)
                x2 = min(frame.shape[1], x2 + padding)
                y2 = min(frame.shape[0], y2 + padding)
                face_img = frame[y1:y2, x1:x2]
                if face_img.shape[0] < 40 or face_img.shape[1] < 40:
                    continue

                yolo_face_path = f"{self.yolo_cropped_dir}/yolo_face_{lecture_id}_{frame_count}_{current_session}.jpg"
                cv2.imwrite(yolo_face_path, face_img)

                face_img_rgb = cv2.cvtColor(face_img, cv2.COLOR_BGR2RGB)
                detected_faces = self.app.get(face_img_rgb)
                if not detected_faces:
                    face_path = f"{self.unknown_faces_dir}/face_{lecture_id}_{frame_count}_unknown.jpg"
                    cv2.imwrite(face_path, face_img_rgb)
                    continue

                face_embedding = detected_faces[0].embedding.flatten()
                student_id, similarity = recognize_face(face_embedding, self.students_embeddings, threshold=0.6)
                detection_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                if student_id != "Unknown":
                    if student_id not in first_detected_at:
                        first_detected_at[student_id] = current_time_str
                    if student_id not in [d["student_id"] for d in detections[current_session]]:
                        face_path = f"{self.known_faces_dir}/face_{lecture_id}_{frame_count}_{student_id}_{similarity:.2f}.jpg"
                        cv2.imwrite(face_path, face_img_rgb)
                        detections[current_session].append({
                            "student_id": student_id,
                            "time": current_time_str,
                            "screenshot_path": screenshot_path
                        })
                        logger.info(f"Detected {student_id} in session {current_session} at {detection_time}, "
                                    f"similarity: {similarity:.2f}")
                else:
                    face_path = f"{self.unknown_faces_dir}/face_{lecture_id}_{frame_count}_unknown.jpg"
                    cv2.imwrite(face_path, face_img_rgb)

                color = (0, 255, 0) if student_id != "Unknown" else (0, 0, 255)
                cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
                cv2.putText(frame, f"{student_id} ({similarity:.2f})", (x1, y1 - 10),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

            cv2.imshow("Face Recognition", frame)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
            time.sleep(1)

        cap.release()
        cv2.destroyAllWindows()
        self.save_final_attendance(lecture_id, course_id, detections, first_detected_at, late_threshold, interval, total_sessions, session_times)

    def save_final_attendance(self, lecture_id, course_id, detections, first_detected_at, late_threshold, interval, total_sessions, session_times):
        all_students = self.students_embeddings.keys()
        session_summary = []

        logger.info("\n=== Session Details ===")
        logger.info(f"Total Sessions: {total_sessions}")
        for i in range(total_sessions):
            start_time = session_times[i]
            end_time = session_times[i + 1] if i < total_sessions - 1 else session_times[i] + (
                late_threshold if i == 0 else interval + 1)
            logger.info(f"Session {i}: {start_time:.2f}s to {end_time:.2f}s")
            session_summary.append({"session_id": i, "start_time": start_time, "end_time": end_time})

        logger.info("\n=== Attendance Summary ===")
        for student_id in all_students:
            status = "Absent"
            sessions = []
            first_check_times = []

            for sess_id in range(total_sessions):
                first_detection_time = "undetected"
                first_check_time = "undetected"

                for detection in detections.get(sess_id, []):
                    if detection["student_id"] == student_id:
                        first_detection_time = detection["time"]
                        first_check_time = detection["time"]
                        if sess_id == 0:
                            status = "Present"
                        elif status != "Present":
                            status = "Late"
                        break

                sessions.append({
                    "sessionId": sess_id,
                    "firstDetectionTime": first_detection_time
                })
                first_check_times.append({
                    "sessionId": sess_id,
                    "firstCheckTime": first_check_time
                })

            attendance_record = {
                "lectureId": lecture_id,
                "studentId": student_id,
                "courseId": course_id,
                "status": status,
                "sessions": sessions,
                "firstCheckTimes": first_check_times,
                "firstDetectedAt": first_detected_at.get(student_id, "undetected")
            }

            logger.info(f"Student: {student_id}, Status: {status}, First Detected: {attendance_record['firstDetectedAt']}")
            self.attendance_collection.update_one(
                {"lecture_id": lecture_id, "student_id": student_id},
                {"$set": attendance_record},
                upsert=True
            )

            try:
                response = requests.post("http://localhost:8080/api/attendances", json=attendance_record)
                if response.status_code == 200:
                    logger.info(f"Backend saved attendance for {student_id}: {response.status_code}")
                else:
                    logger.warning(f"Backend failed for {student_id}: {response.status_code}")
            except requests.exceptions.RequestException as e:
                logger.error(f"Failed to send attendance for {student_id}: {e}")

        logger.info("\n=== Final Summary ===")
        logger.info(f"Total Students: {len(all_students)}")
        present_count = sum(1 for s in all_students if any(
            d["student_id"] == s for d in detections.get(0, [])))
        late_count = sum(1 for s in all_students if any(
            d["student_id"] == s for sess in detections if sess > 0 for d in detections[sess]) and not any(
            d["student_id"] == s for d in detections.get(0, [])))
        absent_count = len(all_students) - present_count - late_count
        logger.info(f"Present: {present_count}, Late: {late_count}, Absent: {absent_count}")

        try:
            response = requests.post(f"http://localhost:8080/api/attendances/finalize/{lecture_id}")
            if response.status_code == 200:
                logger.info(f"Finalized attendance for lecture {lecture_id}")
            else:
                logger.warning(f"Failed to finalize attendance: {response.status_code}")
        except requests.exceptions.RequestException as e:
            logger.error(f"Error finalizing attendance: {e}")

    def fetch_lectures_for_today(self):
        today = datetime.now().strftime("%Y-%m-%d")
        day_name = datetime.now().strftime("%A")
        logger.info(f"Fetching lectures for day: {day_name}, date: {today}")
        lectures = self.courses_collection.find({
            "$or": [
                {"day": {"$regex": f"^{day_name}$", "$options": "i"}},
                {"date": today}
            ]
        })
        lectures_list = list(lectures)
        if lectures_list:
            logger.info(f"Found {len(lectures_list)} lectures for today: {[lecture['_id'] for lecture in lectures_list]}")
        else:
            logger.warning("No lectures found for today")
        return lectures_list

    def schedule_lectures(self):
        lectures = self.fetch_lectures_for_today()
        if not lectures:
            logger.warning("No lectures found for today")
            return
        for lecture in lectures:
            course_id = lecture["_id"]
            start_time = lecture["startTime"]
            end_time = lecture["endTime"]
            late_threshold = lecture.get("lateThreshold", 300)
            interval = lecture.get("interval", 15)

            try:
                start_dt = datetime.strptime(f"{datetime.now().strftime('%Y-%m-%d')} {start_time}", "%Y-%m-%d %H:%M")
                end_dt = datetime.strptime(f"{datetime.now().strftime('%Y-%m-%d')} {end_time}", "%Y-%m-%d %H:%M")
                lecture_duration = int((end_dt - start_dt).total_seconds())
                if lecture_duration <= 0:
                    logger.error(f"Invalid lecture duration for {course_id}")
                    continue

                schedule.every().day.at(start_time).do(
                    self.process_camera,
                    course_id=course_id,
                    lecture_duration=lecture_duration,
                    late_threshold=late_threshold,
                    interval=interval,
                    video_path="rtsp://admin:maysjoelleshouq@192.168.0.109:554/Streaming/Channels/1"
                )
                logger.info(f"Scheduled lecture {course_id} at {start_time}")
            except ValueError as e:
                logger.error(f"Error scheduling lecture {course_id}: {e}")

    def run(self):
        self.schedule_lectures()
        while True:
            schedule.run_pending()
            time.sleep(60)

if __name__ == "__main__":
    system = FaceAttendanceSystem()
    system.run()