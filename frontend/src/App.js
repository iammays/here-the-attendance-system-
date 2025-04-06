import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import WF_Reports from './pages/WF_Reports'; // Make sure this path is correct
import Reset_Password from './pages/Reset_Password';
import Settings from './pages/Settings';

function App() {
  return (
    <Router>
      <Routes>
        {/* Replace Switch with Routes and component with element */}
        <Route path="/" element={<h1>Welcome to the Homepage</h1>} />
        {/* <Route path="/dd" element={<WF_Reports />} />
        <Route path="/d" element={<Reset_Password />} />
        <Route path="/" element={<Settings />} /> */}

      </Routes>
    </Router>
  );
}

export default App;
