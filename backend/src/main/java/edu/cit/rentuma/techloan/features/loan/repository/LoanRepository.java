package edu.cit.rentuma.techloan.features.loan.repository;

import edu.cit.rentuma.techloan.features.loan.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    Optional<Loan> findByReservationId(Long reservationId);
    List<Loan> findByReturnedAtIsNullAndDueDateBefore(LocalDate date);
    List<Loan> findByIsOverdue(Boolean isOverdue);
}
