package com.example.project4.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
}
