package edu.cit.rentuma.techloan.features.penalty.dto;

import java.time.LocalDateTime;

public class PenaltyDTO {

    private Long id;
    private Long loanId;
    private String itemName;
    private Integer daysOverdue;
    private Integer penaltyPoints;
    private LocalDateTime calculatedAt;
    private Boolean paid;

    public PenaltyDTO() {}

    public Long getId()                                    { return id; }
    public void setId(Long id)                             { this.id = id; }
    public Long getLoanId()                                { return loanId; }
    public void setLoanId(Long loanId)                     { this.loanId = loanId; }
    public String getItemName()                            { return itemName; }
    public void setItemName(String itemName)               { this.itemName = itemName; }
    public Integer getDaysOverdue()                        { return daysOverdue; }
    public void setDaysOverdue(Integer daysOverdue)        { this.daysOverdue = daysOverdue; }
    public Integer getPenaltyPoints()                      { return penaltyPoints; }
    public void setPenaltyPoints(Integer penaltyPoints)    { this.penaltyPoints = penaltyPoints; }
    public LocalDateTime getCalculatedAt()                 { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt){ this.calculatedAt = calculatedAt; }
    public Boolean getPaid()                               { return paid; }
    public void setPaid(Boolean paid)                      { this.paid = paid; }
}
