package edu.cit.rentuma.techloan.controller;

import edu.cit.rentuma.techloan.dto.BorrowRequestDTO;
import edu.cit.rentuma.techloan.dto.CreateBorrowRequestDTO;
import edu.cit.rentuma.techloan.factory.DTOFactory;
import edu.cit.rentuma.techloan.model.BorrowRequest;
import edu.cit.rentuma.techloan.model.User;
import edu.cit.rentuma.techloan.observer.BorrowStatus;
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
import java.util.stream.Collectors;

/**
 * Borrow Request REST controller.
 * 
 * Refactoring 4 – Observer Pattern (Behavioral):
 *   This controller manages the full lifecycle of borrow requests.
 *   When status changes, LoanService publishes events that are consumed
 *   by independent listeners (PenaltyListener, AuditListener), demonstrating
 *   the Observer Pattern in action.
 *
 * Refactoring 5 – Factory Pattern (Creational):
 *   Uses DTOFactory to convert domain entities to DTOs, centralizing
 *   conversion logic and improving maintainability.
 *
 * Refactoring 6 – Chain of Responsibility (Behavioral):
 *   Uses borrowRequestValidatorChain to validate requests through
 *   multiple handlers, avoiding nested conditionals.
 */
@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    private final LoanService loanService;
    private final UserRepository userRepository;
    private final DTOFactory dtoFactory;
    private final BorrowRequestValidator borrowRequestValidatorChain;

    public BorrowController(LoanService loanService, UserRepository userRepository,
                           DTOFactory dtoFactory, BorrowRequestValidator borrowRequestValidatorChain) {
        this.loanService = loanService;
        this.userRepository = userRepository;
        this.dtoFactory = dtoFactory;
        this.borrowRequestValidatorChain = borrowRequestValidatorChain;
    }

    /**
     * Create a new borrow request.
     * Returns 201 Created with the new request details.
     * 
     * Uses the Chain of Responsibility pattern to validate the request.
     */
    @PostMapping("/create")
    public ResponseEntity<BorrowRequestDTO> createBorrowRequest(
            @Valid @RequestBody CreateBorrowRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Chain of Responsibility: validate through multiple handlers
            borrowRequestValidatorChain.validate(request);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

        BorrowRequest borrowRequest = loanService.createBorrowRequest(
                user.getId(),
                user.getEmail(),
                request.getItemName(),
                request.getItemDescription(),
                request.getDueDate()
        );

        // Factory Pattern: delegate DTO creation to DTOFactory
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dtoFactory.toBorrowRequestDTO(borrowRequest));
    }

    /**
     * Get all borrow requests for the authenticated user.
     */
    @GetMapping("/my-requests")
    public ResponseEntity<List<BorrowRequestDTO>> getMyBorrowRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));

        List<BorrowRequest> requests = loanService.getUserBorrowRequests(user.getId());
        List<BorrowRequestDTO> dtos = requests.stream()
                .map(dtoFactory::toBorrowRequestDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get a specific borrow request by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BorrowRequestDTO> getBorrowRequest(@PathVariable Long id) {
        BorrowRequest request = loanService.getBorrowRequest(id);
        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(request));
    }

    /**
     * Approve a borrow request (PENDING → APPROVED).
     * This demonstrates the Observer Pattern: status change triggers
     * PenaltyListener and AuditListener reactions.
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<BorrowRequestDTO> approveBorrowRequest(@PathVariable Long id) {
        BorrowRequest request = loanService.updateStatus(id, BorrowStatus.APPROVED);
        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(request));
    }

    /**
     * Mark a borrow request as returned (→ RETURNED).
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowRequestDTO> returnBorrowRequest(@PathVariable Long id) {
        BorrowRequest request = loanService.getBorrowRequest(id);
        request.setReturnDate(java.time.LocalDateTime.now());
        
        BorrowRequest updated = loanService.updateStatus(id, BorrowStatus.RETURNED);
        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(updated));
    }

    /**
     * Mark a borrow request as overdue (→ OVERDUE).
     * This triggers PenaltyListener to increment the user's penalty points.
     * Perfect for testing the Observer Pattern!
     */
    @PutMapping("/{id}/overdue")
    public ResponseEntity<BorrowRequestDTO> markBorrowRequestOverdue(@PathVariable Long id) {
        BorrowRequest request = loanService.updateStatus(id, BorrowStatus.OVERDUE);
        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(request));
    }
}
