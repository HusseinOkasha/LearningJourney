import axios, { Axios, AxiosResponse } from "axios";
import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import Task from "../types/Task";

function TaskComponent() {
  // extract the task uuid.
  const location = useLocation();
  const taskUuid = location.state?.taskUuid;

  // states
  const [task, setTask] = useState<Task>();

  useEffect(() => {
    const fetchData = async () => fetchTask();
    fetchData();
  }, []);

  // method encapsulates the logic of fetching task.
  const fetchTask = async () => {
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
  };

  return (
    <div className="container">
      <div className="row mx-1 my-2 justify-content-center">
        <div className="col-6">
          <p>title: {task?.title}</p>
          <p>description: {task?.description}</p>
          <p>status: {task?.taskStatus}</p>
        </div>
      </div>
    </div>
  );
}

export default TaskComponent;
