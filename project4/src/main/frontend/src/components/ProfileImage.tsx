import { useState } from "react";

const ProfileImage = () => {
  // states
  const [imageState, setImageState] = useState({
    isLoading: true,
    show: true,
  });
  const userProfileId = localStorage.getItem("userProfileId");
  const profileImageUrl: string = `http://localhost:8080/api/profile/${userProfileId}/image/download`;

  return (
    <>
      {imageState.isLoading && <div>please wait we are loading your image</div>}
      {imageState.show && (
        <img
          src={profileImageUrl}
          onLoad={() => {
            setImageState({ ...imageState, isLoading: false });
          }}
          onError={() => {
            setImageState({ isLoading: false, show: false });
          }}
        ></img>
      )}
    </>
  );
};

export default ProfileImage;
