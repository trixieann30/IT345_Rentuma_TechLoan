package edu.cit.rentuma.techloan.features.penalty.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "penalties")
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id", nullable = false, unique = true)
    private Long loanId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_name", length = 255)
    private String itemName;

    @Column(name = "penalty_points", nullable = false)
    private Integer penaltyPoints = 0;

    @Column(name = "days_overdue", nullable = false)
    private Integer daysOverdue = 0;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "paid", nullable = false)
    private Boolean paid = false;

    public Penalty() {}

    public Long getId()                                    { return id; }
    public void setId(Long id)                             { this.id = id; }
    public Long getLoanId()                                { return loanId; }
    public void setLoanId(Long loanId)                     { this.loanId = loanId; }
    public Long getUserId()                                { return userId; }
    public void setUserId(Long userId)                     { this.userId = userId; }
    public String getItemName()                            { return itemName; }
    public void setItemName(String itemName)               { this.itemName = itemName; }
    public Integer getPenaltyPoints()                      { return penaltyPoints; }
    public void setPenaltyPoints(Integer penaltyPoints)    { this.penaltyPoints = penaltyPoints; }
    public Integer getDaysOverdue()                        { return daysOverdue; }
    public void setDaysOverdue(Integer daysOverdue)        { this.daysOverdue = daysOverdue; }
    public LocalDateTime getCalculatedAt()                 { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt){ this.calculatedAt = calculatedAt; }
    public Boolean getPaid()                               { return paid; }
    public void setPaid(Boolean paid)                      { this.paid = paid; }
}
