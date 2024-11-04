import { Navbar, Nav, NavDropdown, Button } from "react-bootstrap";

function NavBar() {
  return (
    <Navbar expand="lg" bg="light" variant="light">
      <Navbar.Brand className="mx-2" href="#">
        Jira
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="basic-navbar-nav" />
      <Navbar.Collapse id="basic-navbar-nav">
        <Nav className="me-auto">
          <Nav.Link href="#">Home</Nav.Link>
          <NavDropdown title="Accounts" id="basic-nav-dropdown">
            <NavDropdown.Item href="#">Admins</NavDropdown.Item>
            <NavDropdown.Item href="#">Employees</NavDropdown.Item>
          </NavDropdown>
          <NavDropdown title="Tasks" id="basic-nav-dropdown">
            <NavDropdown.Item href="#">Create New Task</NavDropdown.Item>
            <NavDropdown.Item href="#">All Tasks </NavDropdown.Item>
            <NavDropdown.Item href="#">My Tasks </NavDropdown.Item>
          </NavDropdown>
        </Nav>
        <Nav className="ms-auto">
          <Button variant="outline-secondary my-2 my-sm-0 mx-2"> Logout</Button>{" "}
        </Nav>
      </Navbar.Collapse>
    </Navbar>
  );
}

export default NavBar;
