package edu.cit.rentuma.techloan.features.reservation.email;

import edu.cit.rentuma.techloan.features.auth.email.EmailService;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowStatus;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowStatusChangedEvent;
import edu.cit.rentuma.techloan.features.reservation.repository.BorrowRequestRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEmailListener {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final BorrowRequestRepository borrowRequestRepository;

    public ReservationEmailListener(EmailService emailService,
                                    UserRepository userRepository,
                                    BorrowRequestRepository borrowRequestRepository) {
        this.emailService             = emailService;
        this.userRepository           = userRepository;
        this.borrowRequestRepository  = borrowRequestRepository;
    }

    @EventListener
    public void onStatusChange(BorrowStatusChangedEvent event) {
        BorrowStatus status = event.getNewStatus();
        if (status != BorrowStatus.APPROVED
                && status != BorrowStatus.REJECTED
                && status != BorrowStatus.OVERDUE) {
            return;
        }

        borrowRequestRepository.findById(event.getBorrowRequestId()).ifPresent(request ->
            userRepository.findByEmail(event.getUserEmail()).ifPresent(user ->
                emailService.sendStatusUpdate(
                    user.getEmail(),
                    user.getFullName(),
                    status.name(),
                    request.getItemName()
                )
            )
        );
    }
}
