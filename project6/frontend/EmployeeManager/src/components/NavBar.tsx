import { useState } from "react";
import { Navbar, Nav, NavDropdown, Button } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";

function NavBar() {
  const navigate = useNavigate();

  // states
  const [isLogedin, setIsLogedin] = useState<boolean>(false);

  // method handles logout
  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    setIsLogedin(false);
    navigate("/");
  };

  // method handle login
  const handleLogin = () => {
    setIsLogedin(true);
    navigate("/login");
  };

  return (
    <Navbar expand="lg" bg="light" variant="light">
      <Navbar.Brand className="mx-2" href="#">
        Jira
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="basic-navbar-nav" />
      <Navbar.Collapse id="basic-navbar-nav">
        {isLogedin && (
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/home">
              Home
            </Nav.Link>
            <NavDropdown title="Accounts" id="basic-nav-dropdown">
              <NavDropdown.Item as={Link} to="/admins">
                Admins
              </NavDropdown.Item>
              <NavDropdown.Item as={Link} to="/employees">
                Employees
              </NavDropdown.Item>
              <NavDropdown.Item as={Link} to="/employees/new">
                Create New Employee
              </NavDropdown.Item>
            </NavDropdown>
            <NavDropdown title="Tasks" id="basic-nav-dropdown">
              <NavDropdown.Item as={Link} to="/new-task">
                Create New Task
              </NavDropdown.Item>
              <NavDropdown.Item href="#">All Tasks </NavDropdown.Item>
              <NavDropdown.Item as={Link} to="/my-tasks">
                My Tasks{" "}
              </NavDropdown.Item>
            </NavDropdown>
          </Nav>
        )}
        {isLogedin && (
          <Nav className="ms-auto">
            <Button
              variant="outline-secondary my-2 my-sm-0 mx-2"
              onClick={handleLogout}
            >
              Logout
            </Button>
          </Nav>
        )}
        {!isLogedin && (
          <Nav className="ms-auto">
            <Button
              variant="outline-secondary my-2 my-sm-0 mx-2"
              onClick={handleLogin}
            >
              Login
            </Button>
          </Nav>
        )}
      </Navbar.Collapse>
    </Navbar>
  );
}

export default NavBar;
