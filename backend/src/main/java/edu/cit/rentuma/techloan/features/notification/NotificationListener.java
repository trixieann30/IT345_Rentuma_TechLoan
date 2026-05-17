package edu.cit.rentuma.techloan.features.notification;

import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowStatusChangedEvent;
import edu.cit.rentuma.techloan.features.reservation.repository.BorrowRequestRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final BorrowRequestRepository borrowRequestRepository;

    public NotificationListener(NotificationService notificationService,
                                 UserRepository userRepository,
                                 BorrowRequestRepository borrowRequestRepository) {
        this.notificationService    = notificationService;
        this.userRepository         = userRepository;
        this.borrowRequestRepository = borrowRequestRepository;
    }

    @EventListener
    public void onStatusChange(BorrowStatusChangedEvent event) {
        User student = userRepository.findByEmail(event.getUserEmail()).orElse(null);
        if (student == null) return;

        String itemName = borrowRequestRepository.findById(event.getBorrowRequestId())
                .map(r -> r.getItemName())
                .orElse("an item");

        switch (event.getNewStatus()) {
            case PENDING -> {
                notificationService.create(student.getId(),
                        "Reservation Submitted",
                        "Your reservation for \"" + itemName + "\" is pending approval.",
                        "RESERVATION_SUBMITTED");

                userRepository.findAll().stream()
                        .filter(u -> u.getRole() == User.Role.CUSTODIAN)
                        .forEach(custodian -> notificationService.create(custodian.getId(),
                                "New Reservation Request",
                                student.getFullName() + " requested \"" + itemName + "\".",
                                "RESERVATION_PENDING"));
            }
            case APPROVED -> notificationService.create(student.getId(),
                    "Reservation Approved",
                    "Your reservation for \"" + itemName + "\" has been approved. Pick it up soon!",
                    "RESERVATION_APPROVED");

            case RELEASED -> notificationService.create(student.getId(),
                    "Item Released",
                    "\"" + itemName + "\" has been handed to you. Please return it by the due date.",
                    "RESERVATION_RELEASED");

            case REJECTED -> notificationService.create(student.getId(),
                    "Reservation Rejected",
                    "Your reservation for \"" + itemName + "\" was rejected.",
                    "RESERVATION_REJECTED");

            case RETURNED -> notificationService.create(student.getId(),
                    "Item Returned",
                    "Your return of \"" + itemName + "\" has been processed. Thank you!",
                    "RESERVATION_RETURNED");

            case OVERDUE -> notificationService.create(student.getId(),
                    "Overdue Alert",
                    "Your loan for \"" + itemName + "\" is overdue. Please return it immediately.",
                    "RESERVATION_OVERDUE");
        }
    }
}
