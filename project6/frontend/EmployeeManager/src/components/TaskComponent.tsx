import axios, { AxiosResponse } from "axios";
import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import Task from "../types/Task";
import { Button } from "react-bootstrap";

function TaskComponent() {
  // extract the task uuid.
  const location = useLocation();
  const taskUuid = location.state?.taskUuid;

  // states
  const [task, setTask] = useState<Task>();
  const [feedback, setFeedback] = useState({ error: "", success: "" });

  useEffect(() => {
    const fetchData = async () => fetchTask();
    fetchData();
  }, []);

  // method encapsulates the logic of fetching task.
  const fetchTask = async () => {
    try {
      // construct the url.
      const url = `http://localhost:8080/api/task/${taskUuid}`;

      // extract the access token.
      const accessToken: string = localStorage.getItem("accessToken");

      // send the request.
      const response: AxiosResponse = await axios.get(url, {
        headers: { Authorization: `Bearer ${accessToken}` },
      });

      // update the state
      setTask(response.data);

      console.log(response);
    } catch (error) {
      if (error.status == 401) {
        setFeedback({
          error: "you are unauthorized to access this page.",
          success: "",
        });
      } else {
        setFeedback({ error: error.message, success: "" });
      }
    }
  };

  // method delete.
  const handleDeleteTask = async () => {
    try {
      // construct the url.
      const url = `http://localhost:8080/api/task/${taskUuid}`;

      // extract the access token.
      const accessToken: string = localStorage.getItem("accessToken");

      // send the request.
      const response: AxiosResponse = await axios.delete(url, {
        headers: { Authorization: `Bearer ${accessToken}` },
      });

      if (response.status == 200) {
        setFeedback({ error: "", success: "Task Deleted Successfully....!" });
      }

      console.log(response);
    } catch (error) {
      if (error.status == 401) {
        setFeedback({
          error: "you are unauthorized to access this page.",
          success: "",
        });
      } else {
        setFeedback({ error: error.message, success: "" });
      }
    }
  };

  return (
    <div className="container">
      <div className="row justify-content-center my-2">
        <div className="col-6">
          {feedback.error && (
            <div className="alert alert-danger">{feedback.error}</div>
          )}

          {feedback.success && (
            <div className="alert alert-success">{feedback.success}</div>
          )}
        </div>
      </div>
      <div className="row mx-1 my-2 justify-content-center">
        <div className="col-6">
          <p>title: {task?.title}</p>
          <p>description: {task?.description}</p>
          <p>status: {task?.taskStatus}</p>
        </div>
      </div>
      <div className="row mx-1 my-2 justify-content-center">
        <div className="col-6">
          <Button variant="outline-secondary "> Edit </Button>
          <Button variant="outline-secondary mx-1" onClick={handleDeleteTask}>
            Delete{" "}
          </Button>
        </div>
      </div>
    </div>
  );
}

export default TaskComponent;
