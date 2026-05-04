package edu.cit.rentuma.techloan.features.penalty;

import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.loan.model.Loan;
import edu.cit.rentuma.techloan.features.loan.repository.LoanRepository;
import edu.cit.rentuma.techloan.features.penalty.model.Penalty;
import edu.cit.rentuma.techloan.features.penalty.repository.PenaltyRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class PenaltyCalculationService {

    private static final int MAX_POINTS_PER_LOAN = 30;

    private final LoanRepository loanRepository;
    private final PenaltyRepository penaltyRepository;
    private final UserRepository userRepository;

    public PenaltyCalculationService(LoanRepository loanRepository,
                                     PenaltyRepository penaltyRepository,
                                     UserRepository userRepository) {
        this.loanRepository    = loanRepository;
        this.penaltyRepository = penaltyRepository;
        this.userRepository    = userRepository;
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void calculateOverduePenalties() {
        List<Loan> overdueLoans =
                loanRepository.findByReturnedAtIsNullAndDueDateBefore(LocalDate.now());
        for (Loan loan : overdueLoans) {
            upsertPenalty(loan);
        }
    }

    @Transactional
    public void calculatePenaltyForLoan(Long loanId) {
        loanRepository.findById(loanId).ifPresent(this::upsertPenalty);
    }

    private void upsertPenalty(Loan loan) {
        long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
        if (daysOverdue <= 0) return;

        int points = (int) Math.min(daysOverdue, MAX_POINTS_PER_LOAN);

        loan.setIsOverdue(true);
        loanRepository.save(loan);

        Penalty penalty = penaltyRepository.findByLoanId(loan.getId())
                .orElseGet(() -> {
                    Penalty p = new Penalty();
                    p.setLoanId(loan.getId());
                    p.setUserId(loan.getUserId());
                    p.setItemName(loan.getItemName());
                    return p;
                });

        penalty.setPenaltyPoints(points);
        penalty.setDaysOverdue((int) daysOverdue);
        penalty.setCalculatedAt(LocalDateTime.now());
        penaltyRepository.save(penalty);

        recalculateUserPoints(loan.getUserId());
    }

    private void recalculateUserPoints(Long userId) {
        int total = penaltyRepository.findByUserIdAndPaid(userId, false)
                .stream()
                .mapToInt(Penalty::getPenaltyPoints)
                .sum();

        Optional<User> userOpt = userRepository.findById(userId);
        userOpt.ifPresent(user -> {
            user.setPenaltyPoints(total);
            userRepository.save(user);
        });
    }
}
