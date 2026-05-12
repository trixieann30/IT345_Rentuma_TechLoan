package edu.cit.rentuma.techloan.features.payment.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    public enum PaymentStatus { PENDING, PAID }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "penalty_id", nullable = false)
    private Long penaltyId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "gateway_session_id")
    private String gatewaySessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public Payment() {}

    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public String getGatewaySessionId()              { return gatewaySessionId; }
    public void setGatewaySessionId(String v)        { this.gatewaySessionId = v; }
    public Long getUserId()                          { return userId; }
    public void setUserId(Long userId)               { this.userId = userId; }
    public Long getPenaltyId()                       { return penaltyId; }
    public void setPenaltyId(Long penaltyId)         { this.penaltyId = penaltyId; }
    public Integer getAmount()                       { return amount; }
    public void setAmount(Integer amount)            { this.amount = amount; }
    public PaymentStatus getStatus()                 { return status; }
    public void setStatus(PaymentStatus status)      { this.status = status; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime t)        { this.createdAt = t; }
    public LocalDateTime getPaidAt()                 { return paidAt; }
    public void setPaidAt(LocalDateTime t)           { this.paidAt = t; }
}
