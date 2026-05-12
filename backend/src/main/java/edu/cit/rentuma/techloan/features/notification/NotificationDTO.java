package edu.cit.rentuma.techloan.features.notification;

import edu.cit.rentuma.techloan.features.notification.model.Notification;

import java.time.LocalDateTime;

public class NotificationDTO {

    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationDTO from(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.id        = n.getId();
        dto.title     = n.getTitle();
        dto.message   = n.getMessage();
        dto.type      = n.getType();
        dto.read      = n.isRead();
        dto.createdAt = n.getCreatedAt();
        return dto;
    }

    public Long getId()                 { return id; }
    public String getTitle()            { return title; }
    public String getMessage()          { return message; }
    public String getType()             { return type; }
    public boolean isRead()             { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
