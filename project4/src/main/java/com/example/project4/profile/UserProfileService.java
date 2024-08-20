package com.example.project4.profile;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.project4.config.bucket.BucketName;
import com.example.project4.filestore.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.*;

@Service
public class UserProfileService {
    // fields
    private final UserProfileRepository userProfileRepository;
    private final FileStore fileStore;

    // constructors
    @Autowired
    public UserProfileService(UserProfileRepository userProfileRepository, FileStore fileStore) {
        this.userProfileRepository = userProfileRepository;
        this.fileStore = fileStore;
    }

    // methods
    public Optional<UserProfile> getUserProfileById(UUID id ){
        return this.userProfileRepository.findById(id);
    }
    public UserProfile save(UserProfile userProfile ){
        return this.userProfileRepository.save(userProfile);
    }
    public void uploadUserProfileImage(UUID userProfileId, MultipartFile file){
        // check if the image is not empty
        isFileEmpty(file);

        // check if the file is an image
        isImage(file);

        // check if the user exists in the database
        UserProfile user = getUserProfileOrThrow(userProfileId);

        // extract meta data
        Map<String, String> metadata = extractMetadata(file);

        try {
            /*
            * This folder will contain all images of that specific user.
            * Create a folder inside the bucket on s3
            * Folder name will be : userProfileId/
            * */
            fileStore
                    .createFolderIfNotExists(
                            BucketName.PROFILE_IMAGE.getBucketName(), String.format("%s/",userProfileId)
                    );
            /*
            * Construct the path which the image will be saved to.
            * path: "userProfileId/ImageName"
            * */
            String path = String.format("%s/%s", userProfileId, file.getOriginalFilename());
            fileStore
                    .save(
                            BucketName.PROFILE_IMAGE.getBucketName(), path,
                            Optional.of(metadata), file.getInputStream()
                    );
            /*
             * save the path of the image to the database
             * path is: "userProfileId/imageName" ex: "1/image.png"
             **/
            user.setUserProfileImageLink(path);
            userProfileRepository.save(user);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public byte [] downloadUserProfileImage(UUID userProfileId){
        // get the user from the database.
        UserProfile user = getUserProfileOrThrow(userProfileId);

        // get the bucket name
        String bucketName = BucketName.PROFILE_IMAGE.getBucketName();

        // download the image from s3
        // path is: ("userProfileId/imageName")
        return user.getUserProfileImageLink().map((path)->this.fileStore.download(bucketName, path) ).orElse(new byte[0]);
    }


    private void isImage(MultipartFile file) {
        if (!Arrays.asList(
                IMAGE_JPEG.getMimeType(),
                IMAGE_PNG.getMimeType(),
                IMAGE_GIF.getMimeType()).contains(file.getContentType())) {
            throw new IllegalStateException("File must be an image [" + file.getContentType() + "]");
        }
    }

    private Map<String, String> extractMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return metadata;
    }

    private void isFileEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot upload empty file [ " + file.getSize() + "]");
        }
    }

    private UserProfile getUserProfileOrThrow(UUID userProfileId){
        return getUserProfileById(userProfileId).orElseThrow(()->new IllegalStateException("user not found"));

    }

}
