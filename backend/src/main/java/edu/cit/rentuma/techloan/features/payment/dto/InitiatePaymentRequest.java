package edu.cit.rentuma.techloan.features.payment.dto;

import jakarta.validation.constraints.NotNull;

public class InitiatePaymentRequest {

    @NotNull(message = "penaltyId is required")
    private Long penaltyId;

    public InitiatePaymentRequest() {}

    public Long getPenaltyId()              { return penaltyId; }
    public void setPenaltyId(Long id)       { this.penaltyId = id; }
}
