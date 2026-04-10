package edu.cit.rentuma.techloan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for creating a new borrow request.
 * Validates input from the client before passing to the service layer.
 */
public class CreateBorrowRequestDTO {

    @NotBlank(message = "Item name is required")
    private String itemName;

    private String itemDescription;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    public CreateBorrowRequestDTO() {}

    public CreateBorrowRequestDTO(String itemName, String itemDescription, LocalDateTime dueDate) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.dueDate = dueDate;
    }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
}
