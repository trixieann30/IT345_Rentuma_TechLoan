package edu.cit.rentuma.techloan.model;

import edu.cit.rentuma.techloan.observer.BorrowStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * BorrowRequest entity representing a reservation/loan of a tech item.
 *
 * CHANGED: Added inventoryId (FK to InventoryItem), quantity, purpose, and
 * returnDate as LocalDate to match the SDD reservation request body:
 *   { inventoryId, quantity, purpose, returnDate }
 *
 * itemName and itemDescription are kept as denormalised cache fields so
 * existing read paths (BorrowRequestDTO) continue to work without a join.
 */
@Entity
@Table(name = "borrow_requests")
public class BorrowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    // --- NEW: FK to inventory_items ---
    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    // --- NEW: quantity requested ---
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    // --- NEW: borrower's stated purpose ---
    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    // Denormalised cache — populated from InventoryItem on creation
    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(name = "item_description", columnDefinition = "TEXT")
    private String itemDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BorrowStatus status = BorrowStatus.PENDING;

    @Column(name = "borrow_date")
    private LocalDateTime borrowDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    // SDD uses returnDate as the date the borrower promises to return
    @Column(name = "return_date")
    private LocalDate returnDate;

    // actualReturnDate is set by the custodian when the item is physically returned
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BorrowRequest() {}

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public Long getId()                                  { return id; }
    public void setId(Long id)                           { this.id = id; }

    public Long getUserId()                              { return userId; }
    public void setUserId(Long userId)                   { this.userId = userId; }

    public String getUserEmail()                         { return userEmail; }
    public void setUserEmail(String userEmail)           { this.userEmail = userEmail; }

    public Long getInventoryId()                         { return inventoryId; }
    public void setInventoryId(Long inventoryId)         { this.inventoryId = inventoryId; }

    public Integer getQuantity()                         { return quantity; }
    public void setQuantity(Integer quantity)            { this.quantity = quantity; }

    public String getPurpose()                           { return purpose; }
    public void setPurpose(String purpose)               { this.purpose = purpose; }

    public String getItemName()                          { return itemName; }
    public void setItemName(String itemName)             { this.itemName = itemName; }

    public String getItemDescription()                   { return itemDescription; }
    public void setItemDescription(String desc)          { this.itemDescription = desc; }

    public BorrowStatus getStatus()                      { return status; }
    public void setStatus(BorrowStatus status)           { this.status = status; }

    public LocalDateTime getBorrowDate()                 { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate)  { this.borrowDate = borrowDate; }

    public LocalDateTime getDueDate()                    { return dueDate; }
    public void setDueDate(LocalDateTime dueDate)        { this.dueDate = dueDate; }

    public LocalDate getReturnDate()                     { return returnDate; }
    public void setReturnDate(LocalDate returnDate)      { this.returnDate = returnDate; }

    public LocalDateTime getActualReturnDate()                      { return actualReturnDate; }
    public void setActualReturnDate(LocalDateTime actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)    { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt()                  { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)    { this.updatedAt = updatedAt; }
}