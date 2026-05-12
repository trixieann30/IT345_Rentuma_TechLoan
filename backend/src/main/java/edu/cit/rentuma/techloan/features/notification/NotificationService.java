package edu.cit.rentuma.techloan.features.notification;

import edu.cit.rentuma.techloan.features.notification.model.Notification;
import edu.cit.rentuma.techloan.features.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public void create(Long userId, String title, String message, String type) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        repository.save(n);
    }

    public List<NotificationDTO> getForUser(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationDTO::from)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        repository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setRead(true);
                repository.save(n);
            }
        });
    }

    @Transactional
    public void markAllRead(Long userId) {
        repository.markAllReadByUserId(userId);
    }
}
