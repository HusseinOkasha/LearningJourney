import axios from "axios";
import { useRef, useState } from "react";
import UserProfile from "./UserProfile";
import { useNavigate } from "react-router-dom";

const CreateUserProfileComponent = () => {
  // states
  const [isLoadingState, setIsLoadingState] = useState<boolean>();
  const [successMessage, setSuccessMessage] = useState<string>();
  const [errorMessage, setErrorMessage] = useState<string>();
  //  reference for username.
  const usernameRef: React.RefObject<HTMLInputElement> = useRef(null);

  const navigate = useNavigate();

  async function onSubmitHandler(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    // get username value
    const username: string = usernameRef.current?.value;

    // construct the url
    const url: string = "http://localhost:8080/api/profile";

    setIsLoadingState(true);
    const response = await axios.post(
      url,
      { username: username },
      {
        headers: {
          "Content-Type": "application/json",
        },
      }
    );
    setIsLoadingState(false);
    if (response.status == 200) {
      setSuccessMessage("profile created succesfully");
      localStorage.setItem("userProfileId", response.data.id);
      console.log(localStorage.getItem("userProfileId"));
      navigate("/profile", { replace: true });
    } else {
      setErrorMessage("Failed to create profile");
    }
  }

  return (
    <div className="container ">
      <div className="row justify-content-center">
        {successMessage && (
          <div className="alert alert-success"> {successMessage}</div>
        )}
        {errorMessage && (
          <div className="alert alert-danger"> {errorMessage}</div>
        )}
        <div className="col-xl-6 col-md-8 align-self-center">
          <div className="container-sm justify-content-center mt-5">
            <div className="row">
              <div className="col align-self-center">
                <h1 className="text-center"> Create Profile </h1>
              </div>
            </div>
            <div className="row">
              <form
                className="col align-self-center"
                onSubmit={onSubmitHandler}
              >
                <label>Username</label>
                <input
                  name="username"
                  className="form-control"
                  id="username"
                  placeholder="user name"
                  ref={usernameRef}
                />
                <button className="btn btn-secondary col-3 mt-2 align-self-center">
                  submit
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
export default CreateUserProfileComponent;
