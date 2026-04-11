package edu.cit.rentuma.techloan.dto;

import edu.cit.rentuma.techloan.observer.BorrowStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for returning borrow/reservation request information to clients.
 *
 * CHANGED: Added inventoryId, quantity, purpose, returnDate (LocalDate) to
 * match updated BorrowRequest model and SDD response contract.
 */
public class BorrowRequestDTO {

    private Long id;
    private Long userId;
    private String userEmail;

    // --- NEW fields ---
    private Long inventoryId;
    private Integer quantity;
    private String purpose;

    private String itemName;
    private String itemDescription;
    private BorrowStatus status;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDate returnDate;
    private LocalDateTime actualReturnDate;
    private LocalDateTime createdAt;

    public BorrowRequestDTO() {}

    public BorrowRequestDTO(Long id, Long userId, String userEmail,
                             Long inventoryId, Integer quantity, String purpose,
                             String itemName, String itemDescription,
                             BorrowStatus status,
                             LocalDateTime borrowDate, LocalDateTime dueDate,
                             LocalDate returnDate, LocalDateTime actualReturnDate,
                             LocalDateTime createdAt) {
        this.id               = id;
        this.userId           = userId;
        this.userEmail        = userEmail;
        this.inventoryId      = inventoryId;
        this.quantity         = quantity;
        this.purpose          = purpose;
        this.itemName         = itemName;
        this.itemDescription  = itemDescription;
        this.status           = status;
        this.borrowDate       = borrowDate;
        this.dueDate          = dueDate;
        this.returnDate       = returnDate;
        this.actualReturnDate = actualReturnDate;
        this.createdAt        = createdAt;
    }

    // Getters / Setters
    public Long getId()                                     { return id; }
    public void setId(Long id)                              { this.id = id; }

    public Long getUserId()                                 { return userId; }
    public void setUserId(Long userId)                      { this.userId = userId; }

    public String getUserEmail()                            { return userEmail; }
    public void setUserEmail(String userEmail)              { this.userEmail = userEmail; }

    public Long getInventoryId()                            { return inventoryId; }
    public void setInventoryId(Long inventoryId)            { this.inventoryId = inventoryId; }

    public Integer getQuantity()                            { return quantity; }
    public void setQuantity(Integer quantity)               { this.quantity = quantity; }

    public String getPurpose()                              { return purpose; }
    public void setPurpose(String purpose)                  { this.purpose = purpose; }

    public String getItemName()                             { return itemName; }
    public void setItemName(String itemName)                { this.itemName = itemName; }

    public String getItemDescription()                      { return itemDescription; }
    public void setItemDescription(String itemDescription)  { this.itemDescription = itemDescription; }

    public BorrowStatus getStatus()                         { return status; }
    public void setStatus(BorrowStatus status)              { this.status = status; }

    public LocalDateTime getBorrowDate()                    { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate)     { this.borrowDate = borrowDate; }

    public LocalDateTime getDueDate()                       { return dueDate; }
    public void setDueDate(LocalDateTime dueDate)           { this.dueDate = dueDate; }

    public LocalDate getReturnDate()                        { return returnDate; }
    public void setReturnDate(LocalDate returnDate)         { this.returnDate = returnDate; }

    public LocalDateTime getActualReturnDate()                       { return actualReturnDate; }
    public void setActualReturnDate(LocalDateTime actualReturnDate)  { this.actualReturnDate = actualReturnDate; }

    public LocalDateTime getCreatedAt()                     { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)       { this.createdAt = createdAt; }
}