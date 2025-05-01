import React from 'react';
import { useTranslation } from 'react-i18next';
import '../cssFolder/ActionButtons.css'; 

const ActionButtons = ({ onExport, onSave, onClearStatuses }) => {
  const { t } = useTranslation();

  return (
    <div className="action-buttons">
      <button className="btn btn-primary" onClick={onSave}>
        {t('save')}
      </button>
      <button className="btn btn-secondary" onClick={onExport}>
        {t('export')}
      </button>
      <button className="btn btn-danger" onClick={onClearStatuses}>
        {t('clear Statuses')}
      </button>
    </div>
  );
};

export default ActionButtons;