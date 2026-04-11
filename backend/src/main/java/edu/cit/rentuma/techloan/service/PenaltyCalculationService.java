package edu.cit.rentuma.techloan.service;

import edu.cit.rentuma.techloan.model.BorrowRequest;
import edu.cit.rentuma.techloan.model.User;
import edu.cit.rentuma.techloan.observer.BorrowStatus;
import edu.cit.rentuma.techloan.repository.BorrowRequestRepository;
import edu.cit.rentuma.techloan.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Penalty Calculation Service
 * Automatically calculates and applies penalty points for overdue items.
 * 
 * Runs periodically (every hour) to check for items past their due date
 * and calculates penalty points based on the number of days overdue.
 */
@Service
public class PenaltyCalculationService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final UserRepository userRepository;

    public PenaltyCalculationService(BorrowRequestRepository borrowRequestRepository,
                                    UserRepository userRepository) {
        this.borrowRequestRepository = borrowRequestRepository;
        this.userRepository = userRepository;
    }

    /**
     * Scheduled task that runs every hour to calculate penalties for overdue items
     * Penalty calculation: 1 point per day overdue
     */
    @Scheduled(fixedRate = 3600000) // Every hour (3600000 ms)
    @Transactional
    public void calculateOverduePenalties() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find all APPROVED items that are past their due date
        List<BorrowRequest> borrowRequests = borrowRequestRepository.findAll();
        
        for (BorrowRequest request : borrowRequests) {
            // Skip if not in APPROVED status (RETURNED and OVERDUE don't need further penalties)
            if (request.getStatus() != BorrowStatus.APPROVED) {
                continue;
            }
            
            // Skip if no due date
            if (request.getDueDate() == null) {
                continue;
            }
            
            // Check if item is overdue
            if (request.getDueDate().isBefore(now)) {
                calculateAndApplyPenalty(request, now);
            }
        }
    }

    /**
     * Calculate penalty points for a specific overdue item and apply to user
     */
    private void calculateAndApplyPenalty(BorrowRequest request, LocalDateTime now) {
        // Calculate days overdue
        long daysOverdue = ChronoUnit.DAYS.between(request.getDueDate(), now);
        
        // Each day overdue = 1 penalty point
        int penaltyPoints = (int) Math.max(1, daysOverdue);
        
        // Get user and update penalty points
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int currentPenalties = user.getPenaltyPoints() != null ? user.getPenaltyPoints() : 0;
            
            // Only add penalty if not already added for this item
            // We can track this by checking if the item has been overdue before
            int newPenalties = currentPenalties + penaltyPoints;
            user.setPenaltyPoints(newPenalties);
            userRepository.save(user);
            
            System.out.println("✓ Penalty calculated: User " + user.getEmail() + 
                             " (+' + penaltyPoints + ' point(s), ' + daysOverdue + ' days overdue)");
        }
    }

    /**
     * Manually calculate penalties for a specific borrow request
     * Called when an item is immediately marked as overdue by custodian
     */
    public void calculatePenaltyForRequest(Long borrowRequestId) {
        Optional<BorrowRequest> requestOpt = borrowRequestRepository.findById(borrowRequestId);
        if (requestOpt.isPresent()) {
            BorrowRequest request = requestOpt.get();
            if (request.getDueDate() != null) {
                calculateAndApplyPenalty(request, LocalDateTime.now());
            }
        }
    }
}
