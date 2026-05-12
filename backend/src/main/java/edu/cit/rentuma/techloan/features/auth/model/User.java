package edu.cit.rentuma.techloan.features.auth.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "student_id", unique = true, length = 20)
    private String studentId;

    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;

    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @Column(name = "institutional_email", length = 150, unique = true)
    private String institutionalEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "penalty_points")
    private Integer penaltyPoints = 0;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "verification_token", length = 100)
    private String verificationToken;

    @Column(name = "password_reset_token", length = 100)
    private String passwordResetToken;

    @Column(name = "password_reset_expiry")
    private java.time.LocalDateTime passwordResetExpiry;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    private User(UserBuilder b) {
        this.id                = b.id;
        this.fullName          = b.fullName;
        this.email             = b.email;
        this.passwordHash      = b.passwordHash;
        this.studentId         = b.studentId;
        this.googleId          = b.googleId;
        this.personalEmail     = b.personalEmail;
        this.role              = b.role;
        this.penaltyPoints     = b.penaltyPoints != null ? b.penaltyPoints : 0;
        this.emailVerified     = b.emailVerified;
        this.verificationToken = b.verificationToken;
        this.createdAt         = b.createdAt;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getFullName()                  { return fullName; }
    public void setFullName(String fullName)     { this.fullName = fullName; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public String getPasswordHash()              { return passwordHash; }
    public void setPasswordHash(String h)        { this.passwordHash = h; }

    public String getStudentId()                 { return studentId; }
    public void setStudentId(String studentId)   { this.studentId = studentId; }

    public String getGoogleId()                  { return googleId; }
    public void setGoogleId(String googleId)     { this.googleId = googleId; }

    public String getPersonalEmail()             { return personalEmail; }
    public void setPersonalEmail(String email)   { this.personalEmail = email; }

    public String getInstitutionalEmail()         { return institutionalEmail; }
    public void setInstitutionalEmail(String email) { this.institutionalEmail = email; }

    public Role getRole()                        { return role; }
    public void setRole(Role role)               { this.role = role; }

    public Integer getPenaltyPoints()              { return penaltyPoints; }
    public void setPenaltyPoints(Integer p)        { this.penaltyPoints = p; }

    public boolean isEmailVerified()               { return Boolean.TRUE.equals(emailVerified); }
    public void setEmailVerified(Boolean v)        { this.emailVerified = v; }

    public String getVerificationToken()                        { return verificationToken; }
    public void setVerificationToken(String t)                  { this.verificationToken = t; }
    public String getPasswordResetToken()                       { return passwordResetToken; }
    public void setPasswordResetToken(String t)                 { this.passwordResetToken = t; }
    public java.time.LocalDateTime getPasswordResetExpiry()     { return passwordResetExpiry; }
    public void setPasswordResetExpiry(java.time.LocalDateTime d) { this.passwordResetExpiry = d; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime d)      { this.createdAt = d; }

    public static class UserBuilder {
        private Long id;
        private String fullName;
        private String email;
        private String passwordHash;
        private String studentId;
        private String googleId;
        private String personalEmail;
        private String institutionalEmail;
        private Role role;
        private Integer penaltyPoints = 0;
        private Boolean emailVerified = false;
        private String verificationToken;
        private LocalDateTime createdAt;

        public UserBuilder id(Long id)                         { this.id = id; return this; }
        public UserBuilder fullName(String v)                  { this.fullName = v; return this; }
        public UserBuilder email(String v)                     { this.email = v; return this; }
        public UserBuilder passwordHash(String v)              { this.passwordHash = v; return this; }
        public UserBuilder studentId(String v)                 { this.studentId = v; return this; }
        public UserBuilder googleId(String v)                  { this.googleId = v; return this; }
        public UserBuilder personalEmail(String v)             { this.personalEmail = v; return this; }
        public UserBuilder institutionalEmail(String v)         { this.institutionalEmail = v; return this; }
        public UserBuilder role(Role v)                        { this.role = v; return this; }
        public UserBuilder penaltyPoints(Integer v)            { this.penaltyPoints = v; return this; }
        public UserBuilder emailVerified(Boolean v)            { this.emailVerified = v; return this; }
        public UserBuilder verificationToken(String v)         { this.verificationToken = v; return this; }
        public UserBuilder createdAt(LocalDateTime v)          { this.createdAt = v; return this; }

        public User build() { return new User(this); }
    }

    public enum Role {
        STUDENT, FACULTY, CUSTODIAN
    }
}
