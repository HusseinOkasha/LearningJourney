import axios from "axios";
import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

function CreateNewEmployeeComponent() {
  // states for the input fields.
  const [name, setName] = useState<string>();
  const [email, setEmail] = useState<string>("");
  const [password, setPassword] = useState<string>("");

  // state for providing feedback to the user.
  const [errors, setErrors] = useState({
    responseError: "",
    name: "",
    email: "",
    password: "",
  });
  const [successMessage, setSuccessMessage] = useState("");

  // state indicates whether ther request is being proccessed or not.
  const [isLoading, setIsLoading] = useState<boolean>(false);

  // Email validation function
  const validateEmail = (email: string) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  // Handle name change
  const handleNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.value.length == 0) {
      setErrors({ ...errors, name: "should not be empty" });
    } else {
      setErrors({ ...errors, name: "" });
    }
    setName(e.target.value);
  };

  // Handle email change
  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setEmail(value);
    const isValid: boolean = validateEmail(value);
    if (!isValid) {
      setErrors({ ...errors, email: "invalid email" });
    } else {
      setErrors({ ...errors, email: "" });
    }
  };

  // Handle password change.
  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.value.length == 0) {
      setErrors({ ...errors, password: "should not be empty" });
    } else {
      setErrors({ ...errors, password: "" });
    }
    setPassword(e.target.value);
  };

  // method encapsulates sending the authentication request logic.
  const sendAuthenticationRequest = async (email: string, password: string) => {
    // build request body.
    const requestBody = {
      name: name,
      email: email,
      password: password,
      role: "EMPLOYEE",
    };

    // extract the access token.
    const accessToken: string = localStorage.getItem("accessToken");
    try {
      // set the is loading state to true.
      setIsLoading(true);

      // Send the request with the Authorization header
      const response = await axios.post(
        "http://localhost:8080/api/admin/employees",
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
      if (response.status == 201) {
        setSuccessMessage("Employee created successfully!!");
        setErrors({ email: "", name: "", password: "", responseError: "" });
      }
    } catch (error) {
      setSuccessMessage("");
      setErrors({ ...errors, responseError: error.message });
    } finally {
      // set the is loading state to false.
      setIsLoading(false);
    }
  };

  // Handle Login
  const handleLogin = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    // send authentication request with the provided email and password.
    sendAuthenticationRequest(email, password);
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
          <h1> Create New Employee Account </h1>
        </div>
      </div>
      <div>
        <form onSubmit={handleLogin}>
          <div className="row justify-content-center">
            <div className="col-6">
              <label> Name </label>
              <input
                type="text"
                className={`form-control ${
                  errors.name.length == 0 ? "" : "is-invalid"
                }`}
                onChange={handleNameChange}
              />
              {errors.name.length > 0 && (
                <div className="invalid-feedback"> {errors.name}</div>
              )}
            </div>
          </div>
          <div className="row justify-content-center">
            <div className="col-6">
              <label> Email </label>
              <input
                type="email"
                className={`form-control ${
                  errors.email.length == 0 ? "" : "is-invalid"
                }`}
                onChange={handleEmailChange}
              />
              {errors.email.length > 0 && (
                <div className="invalid-feedback"> {errors.email}</div>
              )}
            </div>
          </div>
          <div className="row justify-content-center">
            <div className="col-6">
              <label> Password </label>
              <input
                type="password"
                className={`form-control ${
                  errors.password.length == 0 ? "" : "is-invalid"
                }`}
                onChange={handlePasswordChange}
              />
              {errors.password.length > 0 && (
                <div className="invalid-feedback"> {errors.password}</div>
              )}
            </div>
          </div>
          <div className="row justify-content-center mt-2">
            <div className="col-6 text-center">
              <button className="col-3 btn btn-secondary" disabled={isLoading}>
                Create
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CreateNewEmployeeComponent;
