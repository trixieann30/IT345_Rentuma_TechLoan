package edu.cit.rentuma.techloan.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * User entity.
 * Refactoring 1 – Builder Pattern (Creational):
 *   personalEmail is now a first-class field in UserBuilder,
 *   eliminating the reflection workaround in GoogleAuthService.
 */
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

    // --- Builder fix: now a first-class field (no reflection needed) ---
    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "penalty_points")
    private Integer penaltyPoints = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    // Full-args constructor used by the Builder
    private User(UserBuilder b) {
        this.id           = b.id;
        this.fullName     = b.fullName;
        this.email        = b.email;
        this.passwordHash = b.passwordHash;
        this.studentId    = b.studentId;
        this.googleId     = b.googleId;
        this.personalEmail = b.personalEmail;
        this.role          = b.role;
        this.penaltyPoints = b.penaltyPoints != null ? b.penaltyPoints : 0;
        this.createdAt     = b.createdAt;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    // ----------------------------------------------------------------
    // Getters / Setters
    // ----------------------------------------------------------------

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

    public Role getRole()                        { return role; }
    public void setRole(Role role)               { this.role = role; }

    public Integer getPenaltyPoints()            { return penaltyPoints; }
    public void setPenaltyPoints(Integer p)      { this.penaltyPoints = p; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime d)    { this.createdAt = d; }

    // ----------------------------------------------------------------
    // Builder (Creational – Builder Pattern)
    // ----------------------------------------------------------------

    public static class UserBuilder {
        private Long id;
        private String fullName;
        private String email;
        private String passwordHash;
        private String studentId;
        private String googleId;
        private String personalEmail;   // <-- added (was missing before)
        private Role role;
        private Integer penaltyPoints = 0;
        private LocalDateTime createdAt;

        public UserBuilder id(Long id)                         { this.id = id; return this; }
        public UserBuilder fullName(String v)                  { this.fullName = v; return this; }
        public UserBuilder email(String v)                     { this.email = v; return this; }
        public UserBuilder passwordHash(String v)              { this.passwordHash = v; return this; }
        public UserBuilder studentId(String v)                 { this.studentId = v; return this; }
        public UserBuilder googleId(String v)                  { this.googleId = v; return this; }
        public UserBuilder personalEmail(String v)             { this.personalEmail = v; return this; }
        public UserBuilder role(Role v)                        { this.role = v; return this; }
        public UserBuilder penaltyPoints(Integer v)            { this.penaltyPoints = v; return this; }
        public UserBuilder createdAt(LocalDateTime v)          { this.createdAt = v; return this; }

        public User build() { return new User(this); }
    }

    // ----------------------------------------------------------------
    // Role enum
    // ----------------------------------------------------------------

    public enum Role {
        STUDENT, FACULTY, CUSTODIAN
    }
}