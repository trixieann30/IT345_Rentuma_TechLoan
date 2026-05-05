package edu.cit.rentuma.techloan.features.loan;

import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.loan.dto.LoanDTO;
import edu.cit.rentuma.techloan.features.loan.model.Loan;
import edu.cit.rentuma.techloan.features.penalty.model.Penalty;
import edu.cit.rentuma.techloan.features.penalty.repository.PenaltyRepository;
import edu.cit.rentuma.techloan.shared.factory.DTOFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository;
    private final DTOFactory dtoFactory;

    public LoanController(LoanService loanService,
                           UserRepository userRepository,
                           PenaltyRepository penaltyRepository,
                           DTOFactory dtoFactory) {
        this.loanService       = loanService;
        this.userRepository    = userRepository;
        this.penaltyRepository = penaltyRepository;
        this.dtoFactory        = dtoFactory;
    }

    @GetMapping
    public ResponseEntity<List<LoanDTO>> getLoans(
            @RequestParam(required = false) Boolean isOverdue,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);

        List<Loan> loans;
        if (user.getRole() == User.Role.CUSTODIAN) {
            loans = (isOverdue != null)
                    ? loanService.getAllLoans().stream()
                        .filter(l -> l.getIsOverdue().equals(isOverdue))
                        .collect(Collectors.toList())
                    : loanService.getAllLoans();
        } else {
            loans = loanService.getLoansByUser(user.getId());
        }

        List<LoanDTO> dtos = loans.stream()
                .map(loan -> {
                    String borrowerName = userRepository.findById(loan.getUserId())
                            .map(User::getFullName).orElse("Unknown");
                    Integer penaltyPts  = penaltyRepository.findByLoanId(loan.getId())
                            .map(Penalty::getPenaltyPoints).orElse(0);
                    return dtoFactory.toLoanDTO(loan, borrowerName, penaltyPts);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnLoan(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Only custodians can process returns"));
        }

        Loan loan = loanService.returnLoan(id);

        String borrowerName = userRepository.findById(loan.getUserId())
                .map(User::getFullName).orElse("Unknown");
        Optional<Penalty> penalty = penaltyRepository.findByLoanId(loan.getId());
        Integer pts = penalty.map(Penalty::getPenaltyPoints).orElse(0);

        return ResponseEntity.ok(dtoFactory.toLoanDTO(loan, borrowerName, pts));
    }

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isCustodian(UserDetails userDetails) {
        return resolveUser(userDetails).getRole() == User.Role.CUSTODIAN;
    }
}
