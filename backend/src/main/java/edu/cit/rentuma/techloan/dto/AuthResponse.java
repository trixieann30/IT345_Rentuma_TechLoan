package edu.cit.rentuma.techloan.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private boolean success;
    private UserResponse user;
    private String token;
    private String refreshToken;
    private String message;
    private String timestamp;
}
