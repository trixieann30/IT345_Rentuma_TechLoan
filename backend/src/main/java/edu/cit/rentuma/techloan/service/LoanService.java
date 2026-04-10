package edu.cit.rentuma.techloan.service;

import edu.cit.rentuma.techloan.model.BorrowRequest;
import edu.cit.rentuma.techloan.observer.BorrowEventPublisher;
import edu.cit.rentuma.techloan.observer.BorrowStatus;
import edu.cit.rentuma.techloan.repository.BorrowRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Refactoring 4 – Observer Pattern (Behavioral):
 *
 * Manages the borrow-request lifecycle. Status transitions are the only
 * responsibility of this service. Side effects — penalty calculation,
 * audit logging, and future notifications — are handled by independent
 * @EventListener beans that react to {@link
 * edu.cit.rentuma.techloan.observer.BorrowStatusChangedEvent}.
 *
 * LoanService knows nothing about PenaltyListener, AuditListener, or
 * any other downstream consumer. Adding a new reaction to a status
 * change requires only a new @EventListener class — this service never
 * needs to change.
 */
@Service
public class LoanService {

    private final BorrowRequestRepository repository;
    private final BorrowEventPublisher eventPublisher;

    public LoanService(BorrowRequestRepository repository,
                       BorrowEventPublisher eventPublisher) {
        this.repository    = repository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Transitions a BorrowRequest to the given status and publishes a
     * {@link edu.cit.rentuma.techloan.observer.BorrowStatusChangedEvent}
     * so that all registered listeners can react independently.
     *
     * @param id        the ID of the BorrowRequest to update
     * @param newStatus the target status
     * @return the saved BorrowRequest
     */
    public BorrowRequest updateStatus(Long id, BorrowStatus newStatus) {
        BorrowRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DB-002:BorrowRequest not found: " + id));

        request.setStatus(newStatus);
        BorrowRequest saved = repository.save(request);

        // Publish event — LoanService knows nothing about penalties or notifications
        eventPublisher.publishStatusChange(id, newStatus, saved.getUserEmail());

        return saved;
    }

    /**
     * Creates a new borrow request for the given user.
     *
     * @param userId         the user creating the request
     * @param userEmail      the user's email
     * @param itemName       name of the item to borrow
     * @param itemDescription description of the item
     * @param dueDate       when the item must be returned
     * @return the created BorrowRequest in PENDING status
     */
    public BorrowRequest createBorrowRequest(Long userId, String userEmail, String itemName,
                                             String itemDescription, LocalDateTime dueDate) {
        BorrowRequest request = new BorrowRequest();
        request.setUserId(userId);
        request.setUserEmail(userEmail);
        request.setItemName(itemName);
        request.setItemDescription(itemDescription);
        request.setDueDate(dueDate);
        request.setStatus(BorrowStatus.PENDING);
        request.setBorrowDate(LocalDateTime.now());

        return repository.save(request);
    }

    /**
     * Get all borrow requests for a user.
     *
     * @param userId the user ID
     * @return list of BorrowRequests belonging to the user
     */
    public List<BorrowRequest> getUserBorrowRequests(Long userId) {
        return repository.findByUserId(userId);
    }

    /**
     * Get a specific borrow request by ID.
     *
     * @param id the request ID
     * @return the BorrowRequest
     */
    public BorrowRequest getBorrowRequest(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DB-002:BorrowRequest not found: " + id));
    }
}