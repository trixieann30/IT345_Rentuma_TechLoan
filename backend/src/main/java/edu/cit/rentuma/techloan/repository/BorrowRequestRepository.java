package edu.cit.rentuma.techloan.repository;

import edu.cit.rentuma.techloan.model.BorrowRequest;
import edu.cit.rentuma.techloan.observer.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for BorrowRequest entities.
 * Refactoring 4 – Observer Pattern (Behavioral):
 *
 * Provides data access for borrow requests. The repository interface
 * stays minimal, delegating event publishing to LoanService.
 */
@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    /**
     * Find all borrow requests for a given user.
     *
     * @param userId the ID of the user
     * @return list of borrow requests belonging to the user
     */
    List<BorrowRequest> findByUserId(Long userId);

    /**
     * Find all borrow requests with a specific status.
     *
     * @param status the status to filter by
     * @return list of borrow requests with the given status
     */
    List<BorrowRequest> findByStatus(BorrowStatus status);

    /**
     * Find all pending borrow requests for a user.
     *
     * @param userId the ID of the user
     * @param status the status (typically PENDING)
     * @return list of borrow requests with the given status and user
     */
    List<BorrowRequest> findByUserIdAndStatus(Long userId, BorrowStatus status);
}
