import React, { useState } from 'react';
import '../cssFolder/SearchBar.css';

const SearchBar = ({ onSearch, onFilter }) => {
  const [searchTerm, setSearchTerm] = useState('');

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
    onSearch(e.target.value);
  };

  return (
    <div className="search-bar">
      <input
        type="text"
        placeholder="Search by student name or ID"
        value={searchTerm}
        onChange={handleSearch}
        className="search-input"
      />
      <select
        onChange={(e) => onFilter(e.target.value)}
        className="filter-select"
      >
        <option value="All">All</option>
        <option value="Present">Present</option>
        <option value="Absent">Absent</option>
        <option value="Late">Late</option>
        <option value="Excuse">Excuse</option>
      </select>
    </div>
  );
};

export default SearchBar;