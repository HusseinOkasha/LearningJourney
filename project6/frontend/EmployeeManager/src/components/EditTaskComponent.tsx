import axios from "axios";
import React, { useState } from "react";
import TaskStatus from "../enums/TaskStatus";
import { useLocation } from "react-router-dom";
import Task from "../types/Task";

function EditTaskComponents() {
  // create error structure.
  interface errorMessages {
    title: string;
    description: string;
    status: string;
    responseError: string;
  }

  //
  const location = useLocation();
  const taskUuid: string = location.state.taskUuid;
  const task: Task = location.state.task;

  // states
  const [title, setTitle] = useState<string>(task.title);
  const [description, setDescription] = useState<string>(task.description);
  const [status, setStatus] = useState<TaskStatus>(task.taskStatus);
  const [errors, setErrors] = useState<errorMessages>({
    title: "",
    description: "",
    status: "",
    responseError: "",
  });
  const [successMessage, setSucessMessage] = useState<string>("");
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const handleTitleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const input: string = e.target.value;
    setTitle(input);
    if (!input || input.length < 0) {
      setErrors({ ...errors, title: "title shouldn't be empty." });
    } else {
      setErrors({ ...errors, title: "" });
    }
  };

  const handleDescriptionChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const input: string = e.target.value;
    setDescription(input);
    if (!input || input.length < 0) {
      setErrors({ ...errors, description: "description shouldn't be empty." });
    } else {
      setErrors({ ...errors, description: "" });
    }
  };

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const input: TaskStatus = e.target.value as TaskStatus;
    console.log(input);
    setStatus(input);
    if (!input) {
      setStatus(input);
    } else {
      setErrors({ ...errors, status: "task status shouldn't be empty." });
    }
  };

  // method encapsulates sending the authentication request logic.
  const sendEditTaskRequest = async () => {
    try {
      // set the is loading state to true.
      setIsLoading(true);

      // create request body
      const requestBody = {
        title: title,
        description: description,
        status: status,
      };

      // log the request body to the console.
      console.log(requestBody);

      // extract the access token.
      const accessToken: string = localStorage.getItem("accessToken");

      // Send the request with the Authorization header
      const response = await axios.put(
        `http://localhost:8080/api/task/${taskUuid}`,
        requestBody,
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          },
          withCredentials: true,
        }
      );

      // update feedback.
      if (response.status == 200) {
        setSucessMessage("Task saved successfully...");
        setErrors({
          title: "",
          description: "",
          status: "",
          responseError: "",
        });
      } else {
        setErrors({ ...errors, responseError: response.data });
        setSucessMessage("");
      }
      console.log(response);
    } catch (error) {
      console.log(error.response.data);
      setErrors({ ...errors, responseError: error.message });
      setSucessMessage("");
    } finally {
      // set the is loading state to false.
      setIsLoading(false);
    }
  };

  const isvalidData = () => {
    if (title.length == 0 || description.length == 0) {
      setErrors({
        ...errors,
        title: title.length == 0 ? "title should not be empty" : "",
        description:
          description.length == 0 ? "description should not be empty" : "",
        responseError: "invalid data",
      });
      return false;
    }
    return true;
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (isvalidData()) {
      sendEditTaskRequest();
    }
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        {successMessage.length > 0 && (
          <div className="col-6 text-center">
            <p className="alert alert-success"> {successMessage} </p>
          </div>
        )}
        {errors.responseError.length > 0 && (
          <div className="col-6 text-center">
            <p className="alert alert-danger"> {errors.responseError} </p>
          </div>
        )}
      </div>
      <div className="row justify-content-center">
        <div className="col-6 text-center">
          <h1> Edit Task </h1>
        </div>
      </div>
      <div>
        <form onSubmit={handleSubmit}>
          <div className="row justify-content-center">
            <div className="col-6">
              <label> Title </label>
              <input
                type="text"
                value={title}
                className={
                  "form-control " +
                  (errors.title.length > 0 ? "is-invalid" : "")
                }
                onChange={handleTitleChange}
              />
              {errors.title.length > 0 && (
                <div className="invalid-feedback"> {errors.title}</div>
              )}
            </div>
          </div>
          <div className="row justify-content-center">
            <div className="col-6">
              <label> Description </label>
              <input
                type="text-area"
                value={description}
                className={
                  "form-control " +
                  (errors.description.length > 0 ? "is-invalid" : "")
                }
                onChange={handleDescriptionChange}
              />
              {errors.description.length > 0 && (
                <div className="invalid-feedback"> {errors.description}</div>
              )}
            </div>
          </div>
          <div className="row justify-content-center">
            <div className="col-6">
              <label> Status </label>
              <select
                className="form-select"
                id="dropdown"
                onChange={handleStatusChange}
                value={status}
              >
                <option value={TaskStatus.DONE}>Done</option>
                <option value={TaskStatus.IN_PROGRESS}>In progress</option>
                <option value={TaskStatus.TODO}>Todo</option>
              </select>
            </div>
          </div>
          <div className="row justify-content-center mt-2">
            <div className="col-6 text-center">
              <button className="col-3 btn btn-secondary" disabled={isLoading}>
                Save
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EditTaskComponents;
