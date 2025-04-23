import React from 'react';
import '../cssFolder/ActionButtons.css';

const ActionButtons = ({ onAddNewLecture, onExport, onSave, onDelete }) => {
  return (
    <div className="action-buttons">
      <button className="btn btn-success" onClick={onExport}>
        Export to Excel
      </button>
      <button className="btn btn-info" onClick={onSave}>
        Save
      </button>
      <button className="btn btn-danger" onClick={onDelete}>
        Delete
      </button>
    </div>
  );
};

export default ActionButtons;