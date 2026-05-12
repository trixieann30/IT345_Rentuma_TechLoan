package edu.cit.rentuma.techloan.features.auth.dto;

import edu.cit.rentuma.techloan.features.auth.model.User;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String studentId;
    private String personalEmail;
    private String institutionalEmail;
    private boolean emailVerified;
    private User.Role role;
    private Integer penaltyPoints;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public UserResponse(Long id, String fullName, String email, String studentId, String personalEmail,
                        String institutionalEmail, boolean emailVerified, User.Role role,
                        Integer penaltyPoints, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.studentId = studentId;
        this.personalEmail = personalEmail;
        this.institutionalEmail = institutionalEmail;
        this.emailVerified = emailVerified;
        this.role = role;
        this.penaltyPoints = penaltyPoints;
        this.createdAt = createdAt;
    }

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getFullName(), user.getEmail(),
                user.getStudentId(), user.getPersonalEmail(), user.getInstitutionalEmail(),
                user.isEmailVerified(), user.getRole(),
                user.getPenaltyPoints(), user.getCreatedAt());
    }

    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }

    public String getFullName()                      { return fullName; }
    public void setFullName(String fullName)         { this.fullName = fullName; }

    public String getEmail()                         { return email; }
    public void setEmail(String email)               { this.email = email; }

    public String getStudentId()                     { return studentId; }
    public void setStudentId(String studentId)       { this.studentId = studentId; }

    public String getPersonalEmail()                 { return personalEmail; }
    public void setPersonalEmail(String personalEmail) { this.personalEmail = personalEmail; }

    public String getInstitutionalEmail()             { return institutionalEmail; }
    public void setInstitutionalEmail(String institutionalEmail) { this.institutionalEmail = institutionalEmail; }

    public boolean isEmailVerified()                  { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public User.Role getRole()                        { return role; }
    public void setRole(User.Role role)               { this.role = role; }

    public Integer getPenaltyPoints()                { return penaltyPoints; }
    public void setPenaltyPoints(Integer p)           { this.penaltyPoints = p; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime d)         { this.createdAt = d; }
}
