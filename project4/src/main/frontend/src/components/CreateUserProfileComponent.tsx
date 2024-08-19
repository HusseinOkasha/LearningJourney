import axios from "axios";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

const CreateUserProfileComponent = () => {
  // states
  const [formState, setFormState] = useState({
    isLoading: false,
    successMessage: "",
    errorMessage: "",
  });

  const [username, setUsername] = useState("");

  const navigate = useNavigate();

  async function onSubmitHandler(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    // construct the url
    const url: string = "http://localhost:8080/api/profile/";

    // update the form state.
    setFormState({
      ...formState,
      isLoading: true,
      errorMessage: "",
      successMessage: "",
    });

    try {
      // send the request.
      const response = await axios.post(
        url,
        { username: username },
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      // update the form state.
      setFormState({ ...formState, isLoading: false });
      if (response.status === 200) {
        setFormState({
          ...formState,
          successMessage: "User profile created successfully",
        });

        // store the userProfileId in the local storage
        localStorage.setItem("userProfileId", response.data.id);
        // navigate to /profile url
        navigate("/profile", { replace: true });
      } else {
        console.log(response);
        setFormState({
          ...formState,
          errorMessage: "failed to create the profile.",
        });
      }
    } catch (error) {
      setFormState({
        ...formState,
        errorMessage: "failed to create user profile",
      });
    }
  }

  return (
    <div className="container ">
      <div className="row justify-content-center">
        <div className="col-xl-6 col-md-8 align-self-center">
          <div className="container-sm justify-content-center mt-5">
            {formState.successMessage && (
              <div className="alert alert-success">
                {" "}
                {formState.successMessage}
              </div>
            )}
            {formState.errorMessage && (
              <div className="alert alert-danger">
                {" "}
                {formState.errorMessage}
              </div>
            )}
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
                  onChange={(e) => setUsername(e.target.value)}
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
