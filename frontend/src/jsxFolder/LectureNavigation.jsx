import React from 'react';
import { FaArrowLeft, FaArrowRight } from 'react-icons/fa';
import '../cssFolder/LectureNavigation.css';

const LectureNavigation = ({ lectureInfo, currentLectureIndex, totalLectures, onNavigate }) => {
  return (
    <div className="lecture-navigation">
      <div className="lecture-info">
        <span>Date: {lectureInfo.date}</span>
        <span>Time: {lectureInfo.startTime} - {lectureInfo.endTime}</span>
      </div>
      <div className="navigation-buttons">
        <button
          disabled={currentLectureIndex === 0}
          onClick={() => onNavigate('prev')}
        >
          <FaArrowLeft />
        </button>
        <span>{`Lecture ${currentLectureIndex + 1} of ${totalLectures}`}</span>
        <button
          disabled={currentLectureIndex === totalLectures - 1}
          onClick={() => onNavigate('next')}
        >
          <FaArrowRight />
        </button>
      </div>
    </div>
  );
};

export default LectureNavigation;