package edu.cit.rentuma.techloan.model;

import edu.cit.rentuma.techloan.observer.BorrowStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * BorrowRequest entity representing a single loan of a tech item.
 * Refactoring 4 – Observer Pattern (Behavioral):
 *
 * Manages the state of a borrow request throughout its lifecycle.
 * Status transitions trigger {@link edu.cit.rentuma.techloan.observer.BorrowStatusChangedEvent}
 * which is consumed by independent listeners for penalties, audits, and notifications.
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

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BorrowRequest() {}

    // ----------------------------------------------------------------
    // Getters / Setters
    // ----------------------------------------------------------------

    public Long getId()                                 { return id; }
    public void setId(Long id)                          { this.id = id; }

    public Long getUserId()                             { return userId; }
    public void setUserId(Long userId)                  { this.userId = userId; }

    public String getUserEmail()                        { return userEmail; }
    public void setUserEmail(String userEmail)          { this.userEmail = userEmail; }

    public String getItemName()                         { return itemName; }
    public void setItemName(String itemName)            { this.itemName = itemName; }

    public String getItemDescription()                  { return itemDescription; }
    public void setItemDescription(String desc)         { this.itemDescription = desc; }

    public BorrowStatus getStatus()                     { return status; }
    public void setStatus(BorrowStatus status)          { this.status = status; }

    public LocalDateTime getBorrowDate()                { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }

    public LocalDateTime getDueDate()                   { return dueDate; }
    public void setDueDate(LocalDateTime dueDate)       { this.dueDate = dueDate; }

    public LocalDateTime getReturnDate()                { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt()                 { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)   { this.updatedAt = updatedAt; }
}
