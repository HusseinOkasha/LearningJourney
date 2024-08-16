import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

interface UserProfile {
  userProfileId: string;
  username: string;
  userProfileImageLink: string;
}

function UserProfile() {
  let [userProfile, setUserProfile] = useState<UserProfile>();
  const navigate = useNavigate();

  useEffect(() => {
    fetchUserProfile();
  }, []);

  const fetchUserProfile = async () => {
    const profileId = localStorage.getItem("userProfileId");
    const url: string = "http://localhost:8080/api/profile/" + profileId;
    const options = {
      method: "GET",
    };
    const response = await fetch(url, options);
    const jsonRes: UserProfile = await response.json();
    setUserProfile(jsonRes);
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-sm-6 mb-3 mb-sm-0 text-center">
          <h1>Hello {userProfile?.username} </h1>
          <button
            className="btn btn-secondary col mt-2 align-self-center"
            onClick={() => navigate("/profile/image/upload", { replace: true })}
          >
            upload Image
          </button>
        </div>
      </div>
    </div>
  );
}

export default UserProfile;
