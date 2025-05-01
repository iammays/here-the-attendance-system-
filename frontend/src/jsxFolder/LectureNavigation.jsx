import React from 'react';
import { useTranslation } from 'react-i18next';

const LectureNavigation = ({ lectureInfo, currentLectureIndex, totalLectures, onNavigate }) => {
  const { t } = useTranslation();

  return (
    <div className="lecture-navigation">
      <button
        onClick={() => onNavigate('prev')}
        disabled={currentLectureIndex === 0}
        className="btn btn-secondary"
      >
        {t('previous')}
      </button>
      <span>
        {t('lecture')} {currentLectureIndex + 1} {t('of')} {totalLectures}
      </span>
      <button
        onClick={() => onNavigate('next')}
        disabled={currentLectureIndex >= totalLectures - 1}
        className="btn btn-secondary"
      >
        {t('next')}
      </button>
    </div>
  );
};

export default LectureNavigation;