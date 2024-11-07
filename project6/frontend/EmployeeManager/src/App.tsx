import { BrowserRouter, Route, Routes } from "react-router-dom";
import HomeComponent from "./components/HomeComponent";
import NavBar from "./components/NavBar";
import LoginComponent from "./components/LoginComponent";
import AllAdminsComponent from "./components/AllAdminsComponent";
import AccountTasks from "./components/AccountTasks";
import TaskComponent from "./components/TaskComponent";
import AllEmployeesComponent from "./components/AllEmployeesComponent";
import NewTaskComponent from "./components/NewTaskComponent";
import MyTasksComponent from "./components/MyTasksComponent";
import EditTaskComponent from "./components/EditTaskComponent";

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
            <Route path="/new-task" element={<NewTaskComponent />} />
            <Route path="/my-tasks" element={<MyTasksComponent />} />
            <Route path="/edit-task" element={<EditTaskComponent />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </>
  );
}

export default App;
