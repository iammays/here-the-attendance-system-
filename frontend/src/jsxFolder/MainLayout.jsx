import React from 'react';
import Navbar from './Navbar';
import SidebarLayout from './SidebarLayout';
import { Outlet } from 'react-router-dom';

const MainLayout = () => {
  return (
    <>
      <Navbar />
      <SidebarLayout>
        <Outlet />
      </SidebarLayout>
    </>
  );
};

export default MainLayout;