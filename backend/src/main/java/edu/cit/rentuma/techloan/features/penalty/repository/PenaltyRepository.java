package edu.cit.rentuma.techloan.features.penalty.repository;

import edu.cit.rentuma.techloan.features.penalty.model.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
    Optional<Penalty> findByLoanId(Long loanId);
    List<Penalty> findByUserId(Long userId);
    List<Penalty> findByUserIdAndPaid(Long userId, Boolean paid);
}
