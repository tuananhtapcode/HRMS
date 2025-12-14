package com.project.hrms.dto;

import com.project.hrms.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
//@AllArgsConstructor
//@NoArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";

    private Long userId;      // ID của user
    private String username;  // username/email
    private Role role; // list các role của user

    public LoginResponseDTO(String accessToken, Long userId, String username, Role role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
//    public LoginResponseDTO(String accessToken) {
//        this.accessToken = accessToken;
//    }
}