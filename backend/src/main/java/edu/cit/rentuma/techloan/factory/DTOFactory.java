package edu.cit.rentuma.techloan.factory;

import edu.cit.rentuma.techloan.dto.BorrowRequestDTO;
import edu.cit.rentuma.techloan.dto.UserResponse;
import edu.cit.rentuma.techloan.model.BorrowRequest;
import edu.cit.rentuma.techloan.model.User;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern (Creational):
 * Centralises all entity → DTO conversion so controllers
 * never construct DTOs directly.
 *
 * CHANGED: toBorrowRequestDTO now maps inventoryId, quantity, purpose,
 * returnDate and actualReturnDate from the updated BorrowRequest model.
 */
@Component
public class DTOFactory {

    /**
     * Convert a User entity to a UserResponse DTO.
     * Password fields are never included.
     */
    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getStudentId(),
                user.getRole(),
                user.getPenaltyPoints(),
                user.getCreatedAt()
        );
    }

    /**
     * Convert a BorrowRequest entity to a BorrowRequestDTO.
     * Includes all SDD-required fields: inventoryId, quantity, purpose, returnDate.
     *
     * @param borrow the BorrowRequest entity
     * @return BorrowRequestDTO with all relevant fields
     */
    public BorrowRequestDTO toBorrowRequestDTO(BorrowRequest borrow) {
        return new BorrowRequestDTO(
                borrow.getId(),
                borrow.getUserId(),
                borrow.getUserEmail(),
                borrow.getInventoryId(),
                borrow.getQuantity(),
                borrow.getPurpose(),
                borrow.getItemName(),
                borrow.getItemDescription(),
                borrow.getStatus(),
                borrow.getBorrowDate(),
                borrow.getDueDate(),
                borrow.getReturnDate(),
                borrow.getActualReturnDate(),
                borrow.getCreatedAt()
        );
    }
}