package edu.cit.rentuma.techloan.features.reservation;

import edu.cit.rentuma.techloan.features.reservation.model.BorrowRequest;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowStatus;
import edu.cit.rentuma.techloan.features.reservation.repository.BorrowRequestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReservationScheduler {

    private final BorrowRequestRepository borrowRequestRepository;

    public ReservationScheduler(BorrowRequestRepository borrowRequestRepository) {
        this.borrowRequestRepository = borrowRequestRepository;
    }

    // Runs every hour. Rejects PENDING reservations whose return date has passed.
    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void autoRejectExpiredReservations() {
        List<BorrowRequest> expired = borrowRequestRepository
                .findExpiredByStatus(BorrowStatus.PENDING, LocalDate.now());

        for (BorrowRequest req : expired) {
            req.setStatus(BorrowStatus.REJECTED);
            borrowRequestRepository.save(req);
        }

        if (!expired.isEmpty()) {
            System.out.println("[Scheduler] Auto-rejected " + expired.size()
                    + " expired PENDING reservation(s).");
        }
    }
}
