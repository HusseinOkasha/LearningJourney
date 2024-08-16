package com.example.project4.profile;

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

    private final UserProfileRepository userProfileRepository;
    private final FileStore fileStore;


    @Autowired
    public UserProfileService(UserProfileRepository userProfileRepository, FileStore fileStore) {
        this.userProfileRepository = userProfileRepository;
        this.fileStore = fileStore;
    }

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

        //String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getId());
        String path = BucketName.PROFILE_IMAGE.getBucketName();
        String filename = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());

        try {
            fileStore.save(path, filename, Optional.of(metadata), file.getInputStream());
            user.setUserProfileImageLink(filename);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
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
