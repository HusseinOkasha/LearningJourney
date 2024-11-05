import axios from "axios";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

interface Feedback {
  errorMessage: string;
  successMessage: string;
}

function LoginComponent() {
  // states for the input fields.
  const [email, setEmail] = useState<string>("");
  const [password, setPassword] = useState<string>("");

  // state for providing feedback to the user.
  const [feedback, setFeedback] = useState<Feedback>({
    errorMessage: "",
    successMessage: "",
  });

  // state indicates whether the entered email is valid or not.
  const [isValid, setIsValid] = useState<boolean>(true);

  // state indicates whether ther request is being proccessed or not.
  const [isLoading, setIsLoading] = useState<boolean>(false);

  // used for navigation between pages.
  const navigate = useNavigate();

  // Email validation function
  const validateEmail = (email: string) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  // Handle input change
  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setEmail(value);
    const isValid: boolean = validateEmail(value);
    console.log("is email valid: " + isValid);
    setIsValid(isValid);

    if (!isValid) {
      setFeedback({ successMessage: "", errorMessage: "in valid email" });
    } else {
      setFeedback({ successMessage: "", errorMessage: "" });
    }
  };

  // Handle password change.
  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPassword(e.target.value);
  };

  // method encapsulates sending the authentication request logic.
  const sendAuthenticationRequest = async (email: string, password: string) => {
    // Create the Basic Auth token
    const token = btoa(`${email}:${password}`);

    try {
      // set the is loading state to true.
      setIsLoading(true);

      // Send the request with the Authorization header
      const response = await axios.post(
        "http://localhost:8080/api/auth/authenticate",
        {},
        {
          headers: {
            Authorization: `Basic ${token}`,
            "Content-Type": "application/json",
          },
          withCredentials: true,
        }
      );

      // update feedback.
      if (response.status == 200) {
        setFeedback({ successMessage: "Successful Login", errorMessage: "" });
      } else {
        setFeedback({ successMessage: "", errorMessage: "Login Failed" });
      }

      // save the access token in the localStorage
      localStorage.setItem("accessToken", response.data.accessToken);

      // navigate to the home page.
      navigate("/home");
    } catch (error) {
      setFeedback({ successMessage: "", errorMessage: "Login Failed" });
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
        {feedback.successMessage.length > 0 && (
          <div className="col-6 text-center">
            <p className="alert alert-success"> {feedback.successMessage} </p>
          </div>
        )}
        {feedback.errorMessage.length > 0 && (
          <div className="col-6 text-center">
            <p className="alert alert-danger"> {feedback.errorMessage} </p>
          </div>
        )}
      </div>
      <div className="row justify-content-center">
        <div className="col-6 text-center">
          <h1> Login </h1>
        </div>
      </div>
      <div>
        <form onSubmit={handleLogin}>
          <div className="row justify-content-center">
            <div className="col-6">
              <label> Email </label>
              <input
                type="email"
                className={`form-control ${
                  isValid ? "is-valid" : "is-invalid"
                }`}
                onChange={handleEmailChange}
              />
            </div>
          </div>
          <div className="row justify-content-center">
            <div className="col-6">
              <label> Password </label>
              <input
                type="password"
                className="form-control"
                onChange={handlePasswordChange}
              />
            </div>
          </div>
          <div className="row justify-content-center mt-2">
            <div className="col-6 text-center">
              <button className="col-3 btn btn-secondary" disabled={isLoading}>
                Login
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

export default LoginComponent;
