package edu.cit.rentuma.techloan.features.reservation.repository;

import edu.cit.rentuma.techloan.features.reservation.model.BorrowRequest;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findByUserId(Long userId);
    List<BorrowRequest> findByStatus(BorrowStatus status);
    List<BorrowRequest> findByUserIdAndStatus(Long userId, BorrowStatus status);
}
