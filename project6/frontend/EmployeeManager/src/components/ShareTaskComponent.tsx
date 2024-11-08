import { useEffect, useState } from "react";
import { Account } from "../types/Account";

import axios, { AxiosResponse } from "axios";
import { Button } from "react-bootstrap";
import { useLocation } from "react-router-dom";

function ShareTaskComponent() {
  const location = useLocation();
  const taskUuid = location.state.taskUuid;

  // states
  const [employees, setEmployees] = useState<Account[]>([]);
  const [feedback, setFeedback] = useState({});
  const [selectedEmployees, setSelectedEmployees] = useState(new Set());

  useEffect(() => {
    const fetchData = async () => await fetchAllEmployees();
    fetchData();
  }, []);

  // method encapuslates fetching all employees.
  const fetchAllEmployees = async () => {
    // get the access token from the local storage.
    const accessToken: string = localStorage.getItem("accessToken");

    // construct the url.
    const url: string = "http://localhost:8080/api/admin/employees/all";
    try {
      // send the request.
      const response: AxiosResponse = await axios.get(url, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      setEmployees(response.data);
    } catch (error) {
      if (error.status == 401) {
        setFeedback({
          error:
            "Your are unauthorized to access this page, please login with admin account to access it.",
        });
      } else {
        setFeedback({ error: error.message });
      }

      console.log(error);
    }
  };

  // handle check box change.
  const handleCheckBoxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.checked) {
      selectedEmployees.add(e.target.value);
      setSelectedEmployees(new Set(selectedEmployees));
    } else {
      selectedEmployees.delete(e.target.value);
      setSelectedEmployees(new Set(selectedEmployees));
    }
    console.log(selectedEmployees);
  };

  // share task with the selected employees.
  const handleShareTask = () => {
    // handles the click event on the share button.

    // construct the url.
    employees.forEach((employee) => {
      sendShareTaskRequest(employee);
    });
  };

  const sendShareTaskRequest = async (employee) => {
    // construct the url
    const url: string = `http://localhost:8080/api/task/${taskUuid}/accounts/${employee.accountUuid}`;

    // extract the access token.
    const accessToken: string = localStorage.getItem("accessToken");

    try {
      // send the request.
      // second argument(empty object): for the request body.
      await axios.post(
        url,
        {},
        { headers: { Authorization: `Bearer ${accessToken}` } }
      );
    } catch (error) {
      setFeedback({ errorMessage: error.message });
    }
  };

  return (
    <div className="container">
      <div className="row justify-content-center my-2">
        <div className="col-6">
          {feedback.error && (
            <div className="alert alert-danger">{feedback.error}</div>
          )}
        </div>
      </div>
      <div className="row justify-content-center my-2">
        <div className="col-6">
          <h1> Select Employees...</h1>
        </div>
      </div>
      {employees.map((employee) => (
        <div className="row justify-content-center">
          <div className="col-6">
            <div className="form-check">
              <input
                className="form-check-input"
                type="checkbox"
                value={employee.accountUuid}
                id={employee.accountUuid}
                onChange={handleCheckBoxChange}
              />
              <label className="form-check-label">{employee.email}</label>
            </div>
          </div>
        </div>
      ))}
      <div className="row justify-content-center">
        <div className="col-6">
          <Button variant="outline-secondary" onClick={handleShareTask}>
            Share
          </Button>
        </div>
      </div>
    </div>
  );
}

export default ShareTaskComponent;
