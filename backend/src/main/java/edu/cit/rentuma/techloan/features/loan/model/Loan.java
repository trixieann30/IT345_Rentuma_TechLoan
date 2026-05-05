package edu.cit.rentuma.techloan.features.loan.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false, unique = true)
    private Long reservationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "item_name", length = 255)
    private String itemName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "borrowed_at", nullable = false)
    private LocalDateTime borrowedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "is_overdue", nullable = false)
    private Boolean isOverdue = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Loan() {}

    public Long getId()                                  { return id; }
    public void setId(Long id)                           { this.id = id; }
    public Long getReservationId()                       { return reservationId; }
    public void setReservationId(Long reservationId)     { this.reservationId = reservationId; }
    public Long getUserId()                              { return userId; }
    public void setUserId(Long userId)                   { this.userId = userId; }
    public Long getInventoryId()                         { return inventoryId; }
    public void setInventoryId(Long inventoryId)         { this.inventoryId = inventoryId; }
    public String getItemName()                          { return itemName; }
    public void setItemName(String itemName)             { this.itemName = itemName; }
    public Integer getQuantity()                         { return quantity; }
    public void setQuantity(Integer quantity)            { this.quantity = quantity; }
    public LocalDateTime getBorrowedAt()                 { return borrowedAt; }
    public void setBorrowedAt(LocalDateTime borrowedAt)  { this.borrowedAt = borrowedAt; }
    public LocalDate getDueDate()                        { return dueDate; }
    public void setDueDate(LocalDate dueDate)            { this.dueDate = dueDate; }
    public LocalDateTime getReturnedAt()                 { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt)  { this.returnedAt = returnedAt; }
    public Boolean getIsOverdue()                        { return isOverdue; }
    public void setIsOverdue(Boolean isOverdue)          { this.isOverdue = isOverdue; }
    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)    { this.createdAt = createdAt; }
}
