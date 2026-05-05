package edu.cit.rentuma.techloan.features.payment.repository;

import edu.cit.rentuma.techloan.features.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);
    boolean existsByPenaltyIdAndStatus(Long penaltyId, Payment.PaymentStatus status);
}
