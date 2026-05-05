package edu.cit.rentuma.techloan.features.auth.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public RefreshToken() {}

    public RefreshToken(Long userId, String token, LocalDateTime expiresAt) {
        this.userId    = userId;
        this.token     = token;
        this.expiresAt = expiresAt;
    }

    public Long getId()                                { return id; }
    public void setId(Long id)                         { this.id = id; }

    public Long getUserId()                            { return userId; }
    public void setUserId(Long userId)                 { this.userId = userId; }

    public String getToken()                           { return token; }
    public void setToken(String token)                 { this.token = token; }

    public LocalDateTime getExpiresAt()                { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt)  { this.expiresAt = expiresAt; }
}
