package edu.cit.rentuma.techloan.controller;

import edu.cit.rentuma.techloan.dto.BorrowRequestDTO;
import edu.cit.rentuma.techloan.dto.CreateBorrowRequestDTO;
import edu.cit.rentuma.techloan.factory.DTOFactory;
import edu.cit.rentuma.techloan.model.BorrowRequest;
import edu.cit.rentuma.techloan.model.InventoryItem;
import edu.cit.rentuma.techloan.model.User;
import edu.cit.rentuma.techloan.observer.BorrowStatus;
import edu.cit.rentuma.techloan.repository.BorrowRequestRepository;
import edu.cit.rentuma.techloan.repository.InventoryRepository;
import edu.cit.rentuma.techloan.repository.UserRepository;
import edu.cit.rentuma.techloan.service.LoanService;
import edu.cit.rentuma.techloan.validator.BorrowRequestValidator;
import edu.cit.rentuma.techloan.validator.ValidationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reservation REST controller.
 *
 * CHANGED: Base path is now /api/reservations (was /api/borrow) to match the
 * SDD API contract.  The create endpoint now accepts the SDD-compliant body
 * { inventoryId, quantity, purpose, returnDate } and links the request to the
 * actual InventoryItem, decrementing availableQuantity on approval and
 * restoring it on return/rejection.
 *
 * Custodian approve/reject endpoints also send an email trigger stub — replace
 * the TODO comments with your EmailService calls when implemented.
 *
 * Design patterns preserved:
 *   - Observer:            status changes published via LoanService → BorrowEventPublisher
 *   - Factory:             DTOFactory converts entities to response DTOs
 *   - Chain of Responsibility: BorrowRequestValidator still validates the incoming request
 *   - Role-based access:   CUSTODIAN guard on all write actions
 */
@RestController
@RequestMapping("/api/reservations")
public class BorrowController {

    private final LoanService loanService;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final DTOFactory dtoFactory;
    private final BorrowRequestValidator borrowRequestValidatorChain;

    public BorrowController(LoanService loanService,
                             UserRepository userRepository,
                             InventoryRepository inventoryRepository,
                             BorrowRequestRepository borrowRequestRepository,
                             DTOFactory dtoFactory,
                             BorrowRequestValidator borrowRequestValidatorChain) {
        this.loanService                  = loanService;
        this.userRepository               = userRepository;
        this.inventoryRepository          = inventoryRepository;
        this.borrowRequestRepository      = borrowRequestRepository;
        this.dtoFactory                   = dtoFactory;
        this.borrowRequestValidatorChain  = borrowRequestValidatorChain;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/reservations
    // Create a new reservation request (STUDENT / FACULTY only)
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createReservation(
            @Valid @RequestBody CreateBorrowRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Chain of Responsibility validation
        try {
            borrowRequestValidatorChain.validate(request);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }

        User user = resolveUser(userDetails);

        // Role guard: only STUDENT and FACULTY may submit reservations
        if (user.getRole() == User.Role.CUSTODIAN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Custodians cannot submit reservation requests"));
        }

        // Resolve the inventory item and check availability
        InventoryItem item = inventoryRepository.findById(request.getInventoryId())
                .orElse(null);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "BUSINESS-001: Item not found"));
        }
        if (!item.getAvailable() || item.getAvailableQuantity() < request.getQuantity()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "BUSINESS-001: Insufficient item availability"));
        }

        // Create the borrow request (denormalised itemName/description cached from item)
        BorrowRequest borrowRequest = loanService.createBorrowRequest(
                user.getId(),
                user.getEmail(),
                item.getId(),
                request.getQuantity(),
                request.getPurpose(),
                item.getItemName(),
                item.getDescription(),
                request.getReturnDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dtoFactory.toBorrowRequestDTO(borrowRequest));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/reservations  (custodian = all; student = own)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<BorrowRequestDTO>> getReservations(
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);

        List<BorrowRequest> requests;
        if (user.getRole() == User.Role.CUSTODIAN) {
            requests = (status != null)
                    ? borrowRequestRepository.findByStatus(BorrowStatus.valueOf(status.toUpperCase()))
                    : borrowRequestRepository.findAll();
        } else {
            requests = loanService.getUserBorrowRequests(user.getId());
        }

        List<BorrowRequestDTO> dtos = requests.stream()
                .map(dtoFactory::toBorrowRequestDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/reservations/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        BorrowRequest request = loanService.getBorrowRequest(id);
        User user = resolveUser(userDetails);

        // Students can only view their own reservations
        if (user.getRole() != User.Role.CUSTODIAN
                && !request.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }

        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/reservations/{id}/approve  (custodian only)
    // Decrements inventory availableQuantity
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only custodians can approve reservations"));
        }

        BorrowRequest request = loanService.getBorrowRequest(id);

        // Decrement inventory
        inventoryRepository.findById(request.getInventoryId()).ifPresent(item -> {
            int newQty = Math.max(0, item.getAvailableQuantity() - request.getQuantity());
            item.setAvailableQuantity(newQty);
            if (newQty == 0) item.setAvailable(false);
            inventoryRepository.save(item);
        });

        BorrowRequest updated = loanService.updateStatus(id, BorrowStatus.APPROVED);

        // TODO: trigger email notification to borrower (approval)

        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(updated));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/reservations/{id}/reject  (custodian only)
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectReservation(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only custodians can reject reservations"));
        }

        BorrowRequest updated = loanService.updateStatus(id, BorrowStatus.REJECTED);

        // TODO: trigger email notification to borrower (rejection with reason)

        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(updated));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/reservations/{id}/return  (custodian only)
    // Restores inventory availableQuantity
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only custodians can process returns"));
        }

        BorrowRequest request = loanService.getBorrowRequest(id);

        // Restore inventory
        inventoryRepository.findById(request.getInventoryId()).ifPresent(item -> {
            int newQty = item.getAvailableQuantity() + request.getQuantity();
            item.setAvailableQuantity(Math.min(newQty, item.getTotalQuantity()));
            item.setAvailable(true);
            inventoryRepository.save(item);
        });

        // Record actual return timestamp
        request.setActualReturnDate(java.time.LocalDateTime.now());
        borrowRequestRepository.save(request);

        BorrowRequest updated = loanService.updateStatus(id, BorrowStatus.RETURNED);
        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(updated));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/reservations/{id}/overdue  (custodian only)
    // Triggers PenaltyListener via Observer pattern
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}/overdue")
    public ResponseEntity<?> markOverdue(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only custodians can mark items as overdue"));
        }

        BorrowRequest updated = loanService.updateStatus(id, BorrowStatus.OVERDUE);
        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(updated));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
    }

    private boolean isCustodian(UserDetails userDetails) {
        return resolveUser(userDetails).getRole() == User.Role.CUSTODIAN;
    }
}