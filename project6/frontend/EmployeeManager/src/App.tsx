import { BrowserRouter, Route, Routes } from "react-router-dom";
import HomeComponent from "./components/HomeComponent";
import NavBar from "./components/NavBar";
import AccountCardComponent from "./components/AccountCardComponent";
import LoginComponent from "./components/LoginComponent";
import AllAdminsComponent from "./components/AllAdminsComponent";
import AccountTasks from "./components/AccountTasks";
import TaskComponent from "./components/TaskComponent";
import AllEmployeesComponent from "./components/AllEmployeesComponent";

function App() {
  return (
    <>
      <BrowserRouter>
        <NavBar />

        <Routes>
          <Route path="/">
            <Route path="/home" element={<HomeComponent />} />
            <Route path="/admins" element={<AllAdminsComponent />} />
            <Route path="/login" element={<LoginComponent />} />
            <Route path="/account/tasks" element={<AccountTasks />} />
            <Route path="/task" element={<TaskComponent />} />
            <Route path="/employees" element={<AllEmployeesComponent />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </>
  );
}

export default App;
