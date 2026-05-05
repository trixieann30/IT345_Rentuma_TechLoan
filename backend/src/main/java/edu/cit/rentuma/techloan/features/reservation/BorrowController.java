package edu.cit.rentuma.techloan.features.reservation;

import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.inventory.model.InventoryItem;
import edu.cit.rentuma.techloan.features.inventory.repository.InventoryRepository;
import edu.cit.rentuma.techloan.features.loan.LoanService;
import edu.cit.rentuma.techloan.features.reservation.dto.BorrowRequestDTO;
import edu.cit.rentuma.techloan.features.reservation.dto.CreateBorrowRequestDTO;
import edu.cit.rentuma.techloan.features.reservation.model.BorrowRequest;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowStatus;
import edu.cit.rentuma.techloan.features.reservation.repository.BorrowRequestRepository;
import edu.cit.rentuma.techloan.features.reservation.validator.BorrowRequestValidator;
import edu.cit.rentuma.techloan.features.reservation.validator.ValidationException;
import edu.cit.rentuma.techloan.shared.factory.DTOFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class BorrowController {

    private final LoanService loanService;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final DTOFactory dtoFactory;
    private final BorrowRequestValidator borrowRequestValidatorChain;
    private final QrCodeService qrCodeService;
    private final BorrowingSlipService borrowingSlipService;

    public BorrowController(LoanService loanService,
                             UserRepository userRepository,
                             InventoryRepository inventoryRepository,
                             BorrowRequestRepository borrowRequestRepository,
                             DTOFactory dtoFactory,
                             BorrowRequestValidator borrowRequestValidatorChain,
                             QrCodeService qrCodeService,
                             BorrowingSlipService borrowingSlipService) {
        this.loanService                 = loanService;
        this.userRepository              = userRepository;
        this.inventoryRepository         = inventoryRepository;
        this.borrowRequestRepository     = borrowRequestRepository;
        this.dtoFactory                  = dtoFactory;
        this.borrowRequestValidatorChain = borrowRequestValidatorChain;
        this.qrCodeService               = qrCodeService;
        this.borrowingSlipService        = borrowingSlipService;
    }

    @PostMapping
    public ResponseEntity<?> createReservation(
            @Valid @RequestBody CreateBorrowRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            borrowRequestValidatorChain.validate(request);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        User user = resolveUser(userDetails);

        if (user.getRole() == User.Role.CUSTODIAN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Custodians cannot submit reservation requests"));
        }

        InventoryItem item = inventoryRepository.findById(request.getInventoryId()).orElse(null);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "BUSINESS-001: Item not found"));
        }
        if (!item.getAvailable() || item.getAvailableQuantity() < request.getQuantity()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "BUSINESS-001: Insufficient item availability"));
        }

        BorrowRequest borrowRequest = loanService.createBorrowRequest(
                user.getId(), user.getEmail(),
                item.getId(), request.getQuantity(),
                request.getPurpose(),
                item.getItemName(), item.getDescription(),
                request.getReturnDate());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dtoFactory.toBorrowRequestDTO(borrowRequest));
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        BorrowRequest request = loanService.getBorrowRequest(id);
        User user = resolveUser(userDetails);

        if (user.getRole() != User.Role.CUSTODIAN
                && !request.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }

        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(request));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only custodians can approve reservations"));
        }

        BorrowRequest request = loanService.getBorrowRequest(id);

        inventoryRepository.findById(request.getInventoryId()).ifPresent(item -> {
            int newQty = Math.max(0, item.getAvailableQuantity() - request.getQuantity());
            item.setAvailableQuantity(newQty);
            if (newQty == 0) item.setAvailable(false);
            inventoryRepository.save(item);
        });

        BorrowRequest updated = loanService.updateStatus(id, BorrowStatus.APPROVED);
        loanService.createLoan(updated);

        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(updated));
    }

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
        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(updated));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only custodians can process returns"));
        }

        BorrowRequest request = loanService.getBorrowRequest(id);

        inventoryRepository.findById(request.getInventoryId()).ifPresent(item -> {
            int newQty = item.getAvailableQuantity() + request.getQuantity();
            item.setAvailableQuantity(Math.min(newQty, item.getTotalQuantity()));
            item.setAvailable(true);
            inventoryRepository.save(item);
        });

        request.setActualReturnDate(java.time.LocalDateTime.now());
        borrowRequestRepository.save(request);

        BorrowRequest updated = loanService.updateStatus(id, BorrowStatus.RETURNED);
        return ResponseEntity.ok(dtoFactory.toBorrowRequestDTO(updated));
    }

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

    @GetMapping("/{id}/slip")
    public ResponseEntity<?> getReservationSlip(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        BorrowRequest request = loanService.getBorrowRequest(id);
        User user = resolveUser(userDetails);

        if (user.getRole() != User.Role.CUSTODIAN
                && !request.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }

        byte[] pdf = borrowingSlipService.generateSlip(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "borrowing-slip-" + id + ".pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<?> getReservationQr(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        BorrowRequest request = loanService.getBorrowRequest(id);
        User user = resolveUser(userDetails);

        if (user.getRole() != User.Role.CUSTODIAN
                && !request.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }

        byte[] qrPng = qrCodeService.generateQrPng("TECHLOAN-RESERVATION-" + id, 300);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(qrPng, headers, HttpStatus.OK);
    }

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
    }

    private boolean isCustodian(UserDetails userDetails) {
        return resolveUser(userDetails).getRole() == User.Role.CUSTODIAN;
    }
}
