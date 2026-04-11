package edu.cit.rentuma.techloan.service;

import edu.cit.rentuma.techloan.model.BorrowRequest;
import edu.cit.rentuma.techloan.observer.BorrowEventPublisher;
import edu.cit.rentuma.techloan.observer.BorrowStatus;
import edu.cit.rentuma.techloan.repository.BorrowRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages the borrow-request lifecycle.
 *
 * CHANGED: createBorrowRequest now accepts inventoryId, quantity, purpose,
 * and returnDate (LocalDate) to match the updated BorrowRequest model.
 * The controller resolves itemName/description from InventoryItem and passes
 * them here as denormalised cache values.
 *
 * Status transitions are the only responsibility of this service.
 * Side effects (penalty calculation, audit logging) are handled by
 * independent @EventListener beans via BorrowEventPublisher.
 */
@Service
public class LoanService {

    private final BorrowRequestRepository repository;
    private final BorrowEventPublisher eventPublisher;

    public LoanService(BorrowRequestRepository repository,
                       BorrowEventPublisher eventPublisher) {
        this.repository     = repository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Transitions a BorrowRequest to the given status and publishes a
     * BorrowStatusChangedEvent so all registered listeners can react independently.
     */
    public BorrowRequest updateStatus(Long id, BorrowStatus newStatus) {
        BorrowRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DB-002: BorrowRequest not found: " + id));

        request.setStatus(newStatus);
        BorrowRequest saved = repository.save(request);

        eventPublisher.publishStatusChange(id, newStatus, saved.getUserEmail());
        return saved;
    }

    /**
     * Creates a new reservation/borrow request in PENDING status.
     *
     * @param userId          the user creating the request
     * @param userEmail       the user's email
     * @param inventoryId     FK to the InventoryItem being reserved
     * @param quantity        number of units requested
     * @param purpose         borrower's stated purpose/remarks
     * @param itemName        denormalised item name (from InventoryItem)
     * @param itemDescription denormalised item description (from InventoryItem)
     * @param returnDate      date the borrower promises to return the item
     * @return the created BorrowRequest in PENDING status
     */
    public BorrowRequest createBorrowRequest(Long userId, String userEmail,
                                              Long inventoryId, Integer quantity,
                                              String purpose,
                                              String itemName, String itemDescription,
                                              LocalDate returnDate) {
        BorrowRequest request = new BorrowRequest();
        request.setUserId(userId);
        request.setUserEmail(userEmail);
        request.setInventoryId(inventoryId);
        request.setQuantity(quantity);
        request.setPurpose(purpose);
        request.setItemName(itemName);
        request.setItemDescription(itemDescription);
        request.setReturnDate(returnDate);
        request.setStatus(BorrowStatus.PENDING);
        request.setBorrowDate(LocalDateTime.now());

        return repository.save(request);
    }

    /** Get all borrow requests for a specific user. */
    public List<BorrowRequest> getUserBorrowRequests(Long userId) {
        return repository.findByUserId(userId);
    }

    /** Get all PENDING borrow requests (for custodian queue). */
    public List<BorrowRequest> getPendingBorrowRequests() {
        return repository.findByStatus(BorrowStatus.PENDING);
    }

    /** Get a specific borrow request by ID. */
    public BorrowRequest getBorrowRequest(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DB-002: BorrowRequest not found: " + id));
    }
}