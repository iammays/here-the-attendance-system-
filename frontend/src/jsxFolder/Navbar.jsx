import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { FaBell } from 'react-icons/fa';
import { Dropdown } from 'react-bootstrap';
import { IoIosArrowDown } from 'react-icons/io';

const Navbar = () => {
  return (
    <nav className="navbar navbar-expand-lg navbar-light bg-white shadow-sm px-4 py-3">
      <div className="container-fluid">
        <span className="navbar-brand fw-bold text-primary fs-4">HERE</span>

        <div className="d-flex align-items-center ms-auto gap-3">


          {/* Language dropdown */}
          <Dropdown>
            <Dropdown.Toggle
              variant="light"
              className="d-flex align-items-center border-0 p-0 bg-transparent"
            >
              <img
                src="https://flagcdn.com/gb.svg"
                width="24"
                height="16"
                alt="English"
                className="me-1"
              />
              English <IoIosArrowDown className="ms-1" />
            </Dropdown.Toggle>
            <Dropdown.Menu>
              <Dropdown.Item>English</Dropdown.Item>
              <Dropdown.Item>Arabic</Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>

          {/* User icon */}
          <Dropdown align="end">
            <Dropdown.Toggle
              variant="light"
              className="d-flex align-items-center justify-content-center rounded-circle bg-secondary-subtle text-dark"
              style={{ width: '40px', height: '40px', fontWeight: 'bold', fontSize: '1.2rem' }}
            >
              S
            </Dropdown.Toggle>
            <Dropdown.Menu>
              <Dropdown.Item>Profile</Dropdown.Item>
              <Dropdown.Item>Logout</Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
