package edu.cit.rentuma.techloan.features.loan;

import edu.cit.rentuma.techloan.features.inventory.repository.InventoryRepository;
import edu.cit.rentuma.techloan.features.loan.model.Loan;
import edu.cit.rentuma.techloan.features.loan.repository.LoanRepository;
import edu.cit.rentuma.techloan.features.reservation.model.BorrowRequest;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowEventPublisher;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowStatus;
import edu.cit.rentuma.techloan.features.reservation.repository.BorrowRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanService {

    private final BorrowRequestRepository repository;
    private final LoanRepository loanRepository;
    private final InventoryRepository inventoryRepository;
    private final BorrowEventPublisher eventPublisher;

    public LoanService(BorrowRequestRepository repository,
                       LoanRepository loanRepository,
                       InventoryRepository inventoryRepository,
                       BorrowEventPublisher eventPublisher) {
        this.repository          = repository;
        this.loanRepository      = loanRepository;
        this.inventoryRepository = inventoryRepository;
        this.eventPublisher      = eventPublisher;
    }

    public BorrowRequest updateStatus(Long id, BorrowStatus newStatus) {
        BorrowRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DB-002: BorrowRequest not found: " + id));

        request.setStatus(newStatus);
        BorrowRequest saved = repository.save(request);

        try {
            eventPublisher.publishStatusChange(id, newStatus, saved.getUserEmail());
        } catch (Exception e) {
            // Notification/email side-effects must not roll back the status update
        }
        return saved;
    }

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

    @Transactional
    public Loan createLoan(BorrowRequest borrowRequest) {
        return loanRepository.findByReservationId(borrowRequest.getId())
                .orElseGet(() -> {
                    Loan loan = new Loan();
                    loan.setReservationId(borrowRequest.getId());
                    loan.setUserId(borrowRequest.getUserId());
                    loan.setInventoryId(borrowRequest.getInventoryId());
                    loan.setItemName(borrowRequest.getItemName());
                    loan.setQuantity(borrowRequest.getQuantity());
                    loan.setBorrowedAt(LocalDateTime.now());
                    loan.setDueDate(borrowRequest.getReturnDate());
                    loan.setIsOverdue(false);
                    return loanRepository.save(loan);
                });
    }

    @Transactional
    public Loan returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("DB-001: Loan not found: " + loanId));

        loan.setReturnedAt(LocalDateTime.now());
        loan.setIsOverdue(false);
        Loan saved = loanRepository.save(loan);

        inventoryRepository.findById(loan.getInventoryId()).ifPresent(item -> {
            int newQty = item.getAvailableQuantity() + loan.getQuantity();
            item.setAvailableQuantity(Math.min(newQty, item.getTotalQuantity()));
            item.setAvailable(true);
            inventoryRepository.save(item);
        });

        repository.findByUserId(loan.getUserId()).stream()
                .filter(r -> r.getId().equals(loan.getReservationId()))
                .findFirst()
                .ifPresent(r -> {
                    r.setStatus(BorrowStatus.RETURNED);
                    r.setActualReturnDate(LocalDateTime.now());
                    repository.save(r);
                    eventPublisher.publishStatusChange(r.getId(), BorrowStatus.RETURNED, r.getUserEmail());
                });

        return saved;
    }

    public List<BorrowRequest> getUserBorrowRequests(Long userId) {
        return repository.findByUserId(userId);
    }

    public List<BorrowRequest> getPendingBorrowRequests() {
        return repository.findByStatus(BorrowStatus.PENDING);
    }

    public BorrowRequest getBorrowRequest(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DB-002: BorrowRequest not found: " + id));
    }

    public List<Loan> getAllLoans()                      { return loanRepository.findAll(); }
    public List<Loan> getLoansByUser(Long userId)        { return loanRepository.findByUserId(userId); }
    public Loan getLoan(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DB-001: Loan not found: " + id));
    }
}
