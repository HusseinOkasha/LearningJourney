package com.example.project6.dto;

import com.example.project6.Enum.Role;

import java.util.UUID;

public record ProfileDto(String name, String email, UUID accountUuid, Role role) {
}
