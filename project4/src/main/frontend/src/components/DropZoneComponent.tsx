import axios, { AxiosError } from "axios";
import React, { useCallback, useState } from "react";

import { useDropzone } from "react-dropzone";

function DropZoneComponent() {
  const [successMessage, setSuccessMessage] = useState<string>();
  const [errorMessage, setErrorMessage] = useState<string>();

  const onDrop = useCallback(async (acceptedFiles) => {
    // extract the select file
    const file = acceptedFiles[0];

    // create form data object & add the uploaded file to it
    const formData = new FormData();
    formData.append("file", file);

    // Construct the url
    const url: string = `http://localhost:8080/api/profile/${localStorage.getItem(
      "userProfileId"
    )}/image/upload`;
    try {
      const res = await axios.post(url, formData, {
        headers: { "Content-Type": "multuipart/form-data" },
      });
      if (res.status === 200) {
        setErrorMessage("");
        setSuccessMessage("image uploaded successfully");
      } else {
        setSuccessMessage("");
        setErrorMessage("failed to upload the image");
      }
    } catch (error) {
      setSuccessMessage("");
      setErrorMessage("failed to upload the image");
    }
  }, []);
  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

  return (
    <div className="container-sm justify-content-center mt-4">
      <div className="row justify-content-center">
        <div className="card col-lg-6 col-md-8">
          {successMessage && (
            <div className="alert alert-success"> {successMessage}</div>
          )}
          {errorMessage && (
            <div className="alert alert-danger">{errorMessage}</div>
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
