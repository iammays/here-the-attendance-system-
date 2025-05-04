db = connect("mongodb://localhost:27017/backend_db");

// Insert Courses
db.courses.insertMany([
  {
    _id: ObjectId(),
    courseName: "Introduction to AI",
    lateThreshold: 10,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    _id: ObjectId(),
    courseName: "Software Engineering",
    lateThreshold: 15,
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);

// Insert Students
db.students.insertMany([
  {
    _id: ObjectId(),
    studentName: "Alice Smith",
    email: "alice@example.com",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    _id: ObjectId(),
    studentName: "Bob Johnson",
    email: "bob@example.com",
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);

print("✅ Seed data inserted into MongoDB!");
