package edu.cit.rentuma.techloan.features.notification.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }
    public Long getUserId()                     { return userId; }
    public void setUserId(Long userId)          { this.userId = userId; }
    public String getTitle()                    { return title; }
    public void setTitle(String title)          { this.title = title; }
    public String getMessage()                  { return message; }
    public void setMessage(String message)      { this.message = message; }
    public String getType()                     { return type; }
    public void setType(String type)            { this.type = type; }
    public boolean isRead()                     { return read; }
    public void setRead(boolean read)           { this.read = read; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }
}
