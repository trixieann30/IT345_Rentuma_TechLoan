package edu.cit.rentuma.techloan.features.payment;

import edu.cit.rentuma.techloan.features.payment.dto.PaymentDTO;
import edu.cit.rentuma.techloan.features.payment.model.Payment;
import edu.cit.rentuma.techloan.features.payment.repository.PaymentRepository;
import edu.cit.rentuma.techloan.features.penalty.model.Penalty;
import edu.cit.rentuma.techloan.features.penalty.repository.PenaltyRepository;
import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PenaltyRepository penaltyRepository;
    private final UserRepository userRepository;
    private final PayMongoService payMongoService;

    public PaymentService(PaymentRepository paymentRepository,
                          PenaltyRepository penaltyRepository,
                          UserRepository userRepository,
                          PayMongoService payMongoService) {
        this.paymentRepository = paymentRepository;
        this.penaltyRepository = penaltyRepository;
        this.userRepository    = userRepository;
        this.payMongoService   = payMongoService;
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setPenaltyId(penaltyId);
        payment.setAmount(penalty.getPenaltyPoints());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        PayMongoService.CheckoutResult checkout = payMongoService.createCheckoutSession(
                penalty.getPenaltyPoints(), payment.getId(),
                user.getEmail(), user.getFullName());

        payment.setGatewaySessionId(checkout.sessionId());
        paymentRepository.save(payment);

        PaymentDTO dto = PaymentDTO.from(payment);
        dto.setCheckoutUrl(checkout.checkoutUrl());
        return dto;
    }

    @Transactional
    public PaymentDTO confirm(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            throw new IllegalStateException("Payment is already confirmed");
        }

        // PayMongo only redirects to success_url after a successful payment —
        // the redirect itself is the confirmation. Calling verifyPayment() here
        // races against PayMongo's async status update and will often fail.
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
