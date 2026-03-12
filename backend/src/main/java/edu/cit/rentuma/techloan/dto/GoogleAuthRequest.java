package edu.cit.rentuma.techloan.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleAuthRequest {

    @NotBlank(message = "Google ID token is required")
    private String idToken;

    @NotBlank(message = "Role is required")
    private String role;
    
    private String personalEmail;

    public GoogleAuthRequest() {
    }

    public GoogleAuthRequest(String idToken, String role, String personalEmail) {
        this.idToken = idToken;
        this.role = role;
        this.personalEmail = personalEmail;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    public String getPersonalEmail() {
        return personalEmail;
    }
    
    public void setPersonalEmail(String personalEmail) {
        this.personalEmail = personalEmail;
    }
}
