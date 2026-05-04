package edu.cit.rentuma.techloan.features.reservation.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditListener {

    private static final Logger log = LoggerFactory.getLogger(AuditListener.class);

    @EventListener
    public void onStatusChange(BorrowStatusChangedEvent event) {
        log.info("[AUDIT] {} | BorrowRequest#{} transitioned to {} | user={}",
                LocalDateTime.now(),
                event.getBorrowRequestId(),
                event.getNewStatus(),
                event.getUserEmail());
    }
}
