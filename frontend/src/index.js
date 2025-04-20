import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import './i18n';

// إنشاء جذر React
const root = ReactDOM.createRoot(document.getElementById("root"));

// تحميل التطبيق مع BrowserRouter
root.render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
);