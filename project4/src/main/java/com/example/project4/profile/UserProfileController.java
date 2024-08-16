package com.example.project4.profile;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final  UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;

    }
    @PostMapping("/")
    public UserProfile createUserProfile(@RequestBody UserProfile userProfile){
       return this.userProfileService.save(userProfile);
    }

    @PostMapping(
            path = "/{userProfileId}/image/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void uploadUserProfileImage(@PathVariable("userProfileId") UUID userProfileId, @RequestParam("file") MultipartFile file){
        this.userProfileService.uploadUserProfileImage(userProfileId, file);
    }


    @GetMapping("/{userProfileId}")
    public UserProfile getProfile(@PathVariable UUID userProfileId){

        return this.userProfileService.getUserProfileById(userProfileId)
                .orElseThrow(()-> new IllegalStateException("user Profile not found"));
    }

}
