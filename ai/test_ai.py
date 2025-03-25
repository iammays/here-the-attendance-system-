from pymongo import MongoClient

# Connect to MongoDB (assuming it’s running locally on default port 27017)
client = MongoClient("mongodb://localhost:27017/")

# Select the database and collection
db = client["face_attendance"]
collection = db["attendance"]

# Clear existing data (optional, remove this line if you don’t want to reset the collection)
collection.delete_many({})

# Data to insert (without screenshot field)
attendance_data = [
    {
        "lecture_id": "lecture_1",
        "student_id": "202109194",
        "detection_result": "undetected",
        "recognition_result": None,
        "attendance_status": "Absent"
    },
    {
        "lecture_id": "lecture_1",
        "student_id": "202109371",
        "detection_result": "detected",
        "recognition_result": "202109371",
        "attendance_status": "Present"
    },
    {
        "lecture_id": "lecture_1",
        "student_id": "202109943",
        "detection_result": "detected",
        "recognition_result": "202109943",
        "attendance_status": "Present"
    },
    {
        "lecture_id": "lecture_1",
        "student_id": "202109639",
        "detection_result": "detected",
        "recognition_result": "202109639",
        "attendance_status": "Late"
    }
]

# Insert the data into the collection
collection.insert_many(attendance_data)

# Print all documents in the collection
print("Attendance Data in face_attendance.attendance:")
for record in collection.find():
    print(record)

# Close the connection
client.close()