import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

interface UserProfile {
  userProfileId: string;
  username: string;
  profileImageLink: string;
}

function UserProfile() {
  // states
  const [userProfileState, setUserProfileState] = useState({
    isLoading: true,
    errorMessage: "",
    userProfile: null,
  });

  const navigate = useNavigate();

  useEffect(() => {
    fetchUserProfile();
  }, []);

  const fetchUserProfile = async () => {
    // extract user profile id from local storage.
    const profileId = localStorage.getItem("userProfileId");

    // construct url.
    const url: string = "http://localhost:8080/api/profile/" + profileId;

    const options = {
      method: "GET",
    };
    try {
      setUserProfileState({ ...userProfileState, isLoading: true });
      const response = await fetch(url, options);
      setUserProfileState({ ...userProfileState, isLoading: false });
      if (response.status !== 200) {
        setUserProfileState({
          ...userProfileState,
          errorMessage: "failed to load please, try agian later.",
        });
      } else {
        const jsonRes: UserProfile = await response.json();
        setUserProfileState({ ...userProfileState, userProfile: jsonRes });
      }
    } catch (e) {
      console.log(e);
      setUserProfileState({
        ...userProfileState,
        isLoading: false,
        errorMessage: e.message,
      });
    }
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        {userProfileState.userProfile && (
          <div className="col-sm-6 mb-3 mb-sm-0 text-center">
            <h1>Hello {userProfileState.userProfile?.username} </h1>
            <img
              src={`http://localhost:8080/api/profile/${userProfileState.userProfile.id}/image/download`}
            ></img>
            <button
              className="btn btn-secondary col mt-2 align-self-center"
              onClick={() =>
                navigate("/profile/image/upload", { replace: true })
              }
            >
              upload Image
            </button>
          </div>
        )}
        {userProfileState.errorMessage && (
          <div className="alert alert-danger">
            {userProfileState.errorMessage}
          </div>
        )}
      </div>
    </div>
  );
}

export default UserProfile;
