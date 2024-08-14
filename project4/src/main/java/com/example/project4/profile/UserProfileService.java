package com.example.project4.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Autowired
    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public Optional<UserProfile> getUserProfileById(UUID id ){
        return this.userProfileRepository.findById(id);
    }
    public UserProfile save(UserProfile userProfile ){
        return this.userProfileRepository.save(userProfile);
    }

}
