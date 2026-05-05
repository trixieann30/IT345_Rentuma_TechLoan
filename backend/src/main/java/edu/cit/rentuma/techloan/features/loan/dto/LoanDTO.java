package edu.cit.rentuma.techloan.features.loan.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoanDTO {

    private Long id;
    private Long reservationId;
    private Long userId;
    private String borrowerName;
    private Long inventoryId;
    private String itemName;
    private Integer quantity;
    private LocalDateTime borrowedAt;
    private LocalDate dueDate;
    private LocalDateTime returnedAt;
    private Boolean isOverdue;
    private Integer daysOverdue;
    private Integer penaltyPoints;

    public LoanDTO() {}

    public Long getId()                                    { return id; }
    public void setId(Long id)                             { this.id = id; }
    public Long getReservationId()                         { return reservationId; }
    public void setReservationId(Long reservationId)       { this.reservationId = reservationId; }
    public Long getUserId()                                { return userId; }
    public void setUserId(Long userId)                     { this.userId = userId; }
    public String getBorrowerName()                        { return borrowerName; }
    public void setBorrowerName(String borrowerName)       { this.borrowerName = borrowerName; }
    public Long getInventoryId()                           { return inventoryId; }
    public void setInventoryId(Long inventoryId)           { this.inventoryId = inventoryId; }
    public String getItemName()                            { return itemName; }
    public void setItemName(String itemName)               { this.itemName = itemName; }
    public Integer getQuantity()                           { return quantity; }
    public void setQuantity(Integer quantity)              { this.quantity = quantity; }
    public LocalDateTime getBorrowedAt()                   { return borrowedAt; }
    public void setBorrowedAt(LocalDateTime borrowedAt)    { this.borrowedAt = borrowedAt; }
    public LocalDate getDueDate()                          { return dueDate; }
    public void setDueDate(LocalDate dueDate)              { this.dueDate = dueDate; }
    public LocalDateTime getReturnedAt()                   { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt)    { this.returnedAt = returnedAt; }
    public Boolean getIsOverdue()                          { return isOverdue; }
    public void setIsOverdue(Boolean isOverdue)            { this.isOverdue = isOverdue; }
    public Integer getDaysOverdue()                        { return daysOverdue; }
    public void setDaysOverdue(Integer daysOverdue)        { this.daysOverdue = daysOverdue; }
    public Integer getPenaltyPoints()                      { return penaltyPoints; }
    public void setPenaltyPoints(Integer penaltyPoints)    { this.penaltyPoints = penaltyPoints; }
}
