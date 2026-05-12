package edu.cit.rentuma.techloan.features.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleAuthRequest {

    @NotBlank(message = "Google ID token is required")
    private String idToken;

    @NotBlank(message = "Role is required")
    private String role;

    private String institutionalEmail;

    public GoogleAuthRequest() {}

    public GoogleAuthRequest(String idToken, String role, String institutionalEmail) {
        this.idToken = idToken;
        this.role = role;
        this.institutionalEmail = institutionalEmail;
    }

    public String getIdToken()                              { return idToken; }
    public void setIdToken(String idToken)                  { this.idToken = idToken; }

    public String getRole()                                 { return role; }
    public void setRole(String role)                        { this.role = role; }

    public String getInstitutionalEmail()                    { return institutionalEmail; }
    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = institutionalEmail;
    }
}
