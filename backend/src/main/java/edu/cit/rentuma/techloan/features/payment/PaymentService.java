package edu.cit.rentuma.techloan.features.payment;

import edu.cit.rentuma.techloan.features.payment.dto.PaymentDTO;
import edu.cit.rentuma.techloan.features.payment.model.Payment;
import edu.cit.rentuma.techloan.features.payment.repository.PaymentRepository;
import edu.cit.rentuma.techloan.features.penalty.model.Penalty;
import edu.cit.rentuma.techloan.features.penalty.repository.PenaltyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PenaltyRepository penaltyRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          PenaltyRepository penaltyRepository) {
        this.paymentRepository = paymentRepository;
        this.penaltyRepository = penaltyRepository;
    }

    @Transactional
    public PaymentDTO initiate(Long penaltyId, Long userId) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new RuntimeException("Penalty not found: " + penaltyId));

        if (Boolean.TRUE.equals(penalty.getPaid())) {
            throw new IllegalStateException("Penalty is already paid");
        }

        if (paymentRepository.existsByPenaltyIdAndStatus(penaltyId, Payment.PaymentStatus.PENDING)) {
            throw new IllegalStateException("A pending payment already exists for this penalty");
        }

        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setPenaltyId(penaltyId);
        payment.setAmount(penalty.getPenaltyPoints());
        payment.setStatus(Payment.PaymentStatus.PENDING);

        return PaymentDTO.from(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentDTO confirm(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            throw new IllegalStateException("Payment is already confirmed");
        }

        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        penaltyRepository.findById(payment.getPenaltyId()).ifPresent(penalty -> {
            penalty.setPaid(true);
            penaltyRepository.save(penalty);
        });

        return PaymentDTO.from(payment);
    }

    public List<PaymentDTO> getHistory(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(PaymentDTO::from)
                .collect(Collectors.toList());
    }
}
