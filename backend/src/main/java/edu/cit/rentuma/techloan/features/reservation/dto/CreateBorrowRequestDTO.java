package edu.cit.rentuma.techloan.features.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateBorrowRequestDTO {

    @NotNull(message = "Inventory item ID is required")
    private Long inventoryId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String purpose;

    @NotNull(message = "Return date is required")
    @Future(message = "Return date must be a future date")
    private LocalDate returnDate;

    public CreateBorrowRequestDTO() {}

    public CreateBorrowRequestDTO(Long inventoryId, Integer quantity,
                                   String purpose, LocalDate returnDate) {
        this.inventoryId = inventoryId;
        this.quantity    = quantity;
        this.purpose     = purpose;
        this.returnDate  = returnDate;
    }

    public Long getInventoryId()                 { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Integer getQuantity()                 { return quantity; }
    public void setQuantity(Integer quantity)    { this.quantity = quantity; }
    public String getPurpose()                   { return purpose; }
    public void setPurpose(String purpose)       { this.purpose = purpose; }
    public LocalDate getReturnDate()             { return returnDate; }
    public void setReturnDate(LocalDate d)       { this.returnDate = d; }
}
