package com.example.project4.profile;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final  UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;

    }
    @PostMapping
    public UserProfile createUserProfile(@RequestBody UserProfile userProfile){
       return this.userProfileService.save(userProfile);
    }

    @GetMapping("/{userProfileId}")
    public UserProfile getProfile(@PathVariable UUID userProfileId){

        return this.userProfileService.getUserProfileById(userProfileId)
                .orElseThrow(()-> new IllegalStateException("user Profile not found"));
    }
}
