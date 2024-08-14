import { useEffect, useState } from "react";

interface UserProfile {
  userProfileId: string;
  username: string;
  userProfileImageLink: string;
}

function UserProfile() {
  let [userProfile, setUserProfile] = useState<UserProfile>();

  useEffect(() => {
    fetchUserProfile();
  }, []);

  const fetchUserProfile = async () => {
    const profileId = "889aa5fd-87be-40ba-9f77-c43ad7d75822";
    const url: string = "http://localhost:8080/api/profile/" + profileId;
    const options = {
      method: "GET",
    };
    const response = await fetch(url, options);
    const jsonRes: UserProfile = await response.json();
    setUserProfile(jsonRes);
  };

  return (
    <div>
      <div className="row">
        <div className="col-sm-6 mb-3 mb-sm-0">
          <div className="card">
            <div className="card-body">
              {userProfile && (
                <p className="card-text"> {userProfile.username} </p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default UserProfile;
