package edu.cit.rentuma.techloan.factory;

import edu.cit.rentuma.techloan.dto.BorrowRequestDTO;
import edu.cit.rentuma.techloan.dto.UserResponse;
import edu.cit.rentuma.techloan.model.BorrowRequest;
import edu.cit.rentuma.techloan.model.User;
import org.springframework.stereotype.Component;

/**
 * Refactoring 5 – Factory Pattern (Creational):
 *
 * Centralizes the creation of DTOs from domain entities. This abstracts
 * the conversion logic, making it easy to change DTO construction rules
 * without modifying multiple places in the codebase.
 *
 * Benefits:
 *   - Single responsibility: DTOFactory only creates DTOs
 *   - Easy to test: Mock DTOFactory in unit tests
 *   - Centralized logic: Change DTO format in one place
 *   - Maintainable: Add new DTO types by extending the factory
 *
 * Real-world use: Object builders, response mappers, serialization factories
 */
@Component
public class DTOFactory {

    /**
     * Convert a User entity to UserResponse DTO
     *
     * @param user the User entity
     * @return UserResponse DTO with sanitized fields
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
     * Convert a BorrowRequest entity to BorrowRequestDTO
     *
     * @param borrow the BorrowRequest entity
     * @return BorrowRequestDTO with all relevant fields
     */
    public BorrowRequestDTO toBorrowRequestDTO(BorrowRequest borrow) {
        return new BorrowRequestDTO(
                borrow.getId(),
                borrow.getUserId(),
                borrow.getUserEmail(),
                borrow.getItemName(),
                borrow.getItemDescription(),
                borrow.getStatus(),
                borrow.getBorrowDate(),
                borrow.getDueDate(),
                borrow.getReturnDate(),
                borrow.getCreatedAt()
        );
    }
}
