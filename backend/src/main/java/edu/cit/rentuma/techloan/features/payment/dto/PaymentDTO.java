package edu.cit.rentuma.techloan.features.payment.dto;

import edu.cit.rentuma.techloan.features.payment.model.Payment;

import java.time.LocalDateTime;

public class PaymentDTO {

    private Long id;
    private Long userId;
    private Long penaltyId;
    private Integer amount;
    private String status;
    private String gatewaySessionId;
    private String checkoutUrl;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public PaymentDTO() {}

    public static PaymentDTO from(Payment p) {
        PaymentDTO dto = new PaymentDTO();
        dto.id               = p.getId();
        dto.userId           = p.getUserId();
        dto.penaltyId        = p.getPenaltyId();
        dto.amount           = p.getAmount();
        dto.status           = p.getStatus().name();
        dto.gatewaySessionId = p.getGatewaySessionId();
        dto.createdAt        = p.getCreatedAt();
        dto.paidAt           = p.getPaidAt();
        return dto;
    }

    public Long getId()                   { return id; }
    public Long getUserId()               { return userId; }
    public Long getPenaltyId()            { return penaltyId; }
    public Integer getAmount()            { return amount; }
    public String getStatus()             { return status; }
    public String getGatewaySessionId()   { return gatewaySessionId; }
    public String getCheckoutUrl()        { return checkoutUrl; }
    public void setCheckoutUrl(String u)  { this.checkoutUrl = u; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getPaidAt()      { return paidAt; }
}
