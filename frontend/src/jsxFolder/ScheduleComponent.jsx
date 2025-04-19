import React, { useState } from "react";
import "../cssFolder/ScheduleComponent.css";

function ScheduleComponent() {
  const [showModal, setShowModal] = useState(false);
  const [selectedWeek, setSelectedWeek] = useState(null);

  const weeks = [
    { week: 5, days: ["Thursday", "Friday", "Monday"] },
    { week: 4, days: [] },
    { week: 3, days: [] },
    // أضف باقي الأسابيع حسب الحاجة
  ];

    return (
      <div className="modal-overlay">
        <div className="modal-content">
          <h2>Add new day in week {selectedWeek}</h2>
  
          <div className="modal-field">
            <label>Date:</label>
            <input type="date" />
          </div>

          <div className="modal-field">
            <label>Number of lecture:</label>
            <select>
              <option>1</option>
              <option>2</option>
              <option>3</option>
            </select>
          </div>
  
          <div className="modal-field">
            <label>Lecture 1 time:</label>
            <input type="time" /> to <input type="time" />
          </div>
  
          <button className="save-button">save day</button>
  
          <p className="note">
            <span style={{ color: "red" }}>*</span> Just to let you know, you should take the attendance manually on that day.
          </p>
  
          <button className="close-button" onClick={() => setShowModal(false)}>
            Cancel
          </button>
        </div>
      </div>
    );
  }

export default ScheduleComponent;