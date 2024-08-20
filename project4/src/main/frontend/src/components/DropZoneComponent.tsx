import axios from "axios";
import { useCallback, useState } from "react";

import { useDropzone } from "react-dropzone";

function DropZoneComponent() {
  // states
  const [uploadState, setUploadState] = useState({
    isLoading: false,
    successMessage: "",
    errorMessage: "",
  });

  const onDrop = useCallback(async (acceptedFiles) => {
    // reset the states.
    setUploadState({
      isLoading: false,
      errorMessage: "",
      successMessage: "image uploaded sucessfully.",
    });

    // extract the select file
    const file = acceptedFiles[0];

    // create form data object & add the uploaded file to it
    const formData = new FormData();
    formData.append("file", file);

    // Construct the url
    const url: string = `http://localhost:8080/api/profile/${localStorage.getItem(
      "userProfileId"
    )}/image/upload`;

    // send the request.
    try {
      setUploadState({ ...uploadState, isLoading: true });
      const res = await axios.post(url, formData, {
        headers: { "Content-Type": "multuipart/form-data" },
      });
      if (res.status === 200) {
        setUploadState({
          isLoading: false,
          errorMessage: "",
          successMessage: "image uploaded sucessfully.",
        });
      } else {
        setUploadState({
          ...uploadState,
          errorMessage: "failed to upload image",
          successMessage: "",
        });
      }
    } catch (error) {
      setUploadState({
        ...uploadState,
        errorMessage: "failed to upload image",
        successMessage: "",
      });
    }
  }, []);
  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

  return (
    <div className="container-sm justify-content-center mt-4">
      <div className="row justify-content-center">
        <div className="card col-lg-6 col-md-8">
          {uploadState.isLoading && (
            <div className="alert alert-info">uploading...</div>
          )}
          {uploadState.successMessage && (
            <div className="alert alert-success">
              {" "}
              {uploadState.successMessage}
            </div>
          )}
          {uploadState.errorMessage && (
            <div className="alert alert-danger">{uploadState.errorMessage}</div>
          )}
          <h2 className="text-center">upload profile image </h2>
          <div className="card-body">
            <div {...getRootProps()}>
              <input {...getInputProps()} />
              {isDragActive ? (
                <p>Drop the files here ...</p>
              ) : (
                <p>Drag 'n' drop some files here, or click to select files</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default DropZoneComponent;
