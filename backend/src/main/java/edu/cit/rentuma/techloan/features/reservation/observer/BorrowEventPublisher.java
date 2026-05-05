package edu.cit.rentuma.techloan.features.reservation.observer;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class BorrowEventPublisher {

    private final ApplicationEventPublisher publisher;

    public BorrowEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishStatusChange(Long borrowRequestId,
                                    BorrowStatus newStatus,
                                    String userEmail) {
        publisher.publishEvent(
                new BorrowStatusChangedEvent(borrowRequestId, newStatus, userEmail));
    }
}
