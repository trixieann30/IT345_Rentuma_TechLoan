package edu.cit.rentuma.techloan.features.reservation;

import edu.cit.rentuma.techloan.features.reservation.dto.CreateBorrowRequestDTO;
import edu.cit.rentuma.techloan.features.reservation.validator.BorrowRequestValidator;
import edu.cit.rentuma.techloan.features.reservation.validator.DescriptionValidator;
import edu.cit.rentuma.techloan.features.reservation.validator.DueDateValidator;
import edu.cit.rentuma.techloan.features.reservation.validator.ItemNameValidator;
import edu.cit.rentuma.techloan.features.reservation.validator.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TC-DP: Chain of Responsibility — BorrowRequest Validator Tests")
class BorrowRequestValidatorTest {

    private BorrowRequestValidator validatorChain;

    @BeforeEach
    void setUp() {
        ItemNameValidator itemNameValidator   = new ItemNameValidator();
        DueDateValidator dueDateValidator     = new DueDateValidator();
        DescriptionValidator descValidator    = new DescriptionValidator();

        itemNameValidator.setNext(dueDateValidator);
        dueDateValidator.setNext(descValidator);

        validatorChain = itemNameValidator;
    }

    // TC-DP-001
    @Test
    @DisplayName("TC-DP-001: ItemNameValidator throws ValidationException when inventoryId is null")
    void validate_nullInventoryId_throwsValidationException() {
        CreateBorrowRequestDTO req = new CreateBorrowRequestDTO(
                null, 1, "purpose", LocalDate.now().plusDays(3));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> validatorChain.validate(req));
        assertTrue(ex.getMessage().contains("inventory item ID"));
    }

    // TC-DP-002
    @Test
    @DisplayName("TC-DP-002: DueDateValidator throws ValidationException for past return date")
    void validate_pastReturnDate_throwsValidationException() {
        CreateBorrowRequestDTO req = new CreateBorrowRequestDTO(
                1L, 1, "purpose", LocalDate.now().minusDays(1));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> validatorChain.validate(req));
        assertTrue(ex.getMessage().contains("future"));
    }

    // TC-DP-003
    @Test
    @DisplayName("TC-DP-003: DueDateValidator throws ValidationException when return date exceeds 6 months")
    void validate_returnDateBeyond6Months_throwsValidationException() {
        CreateBorrowRequestDTO req = new CreateBorrowRequestDTO(
                1L, 1, "purpose", LocalDate.now().plusMonths(7));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> validatorChain.validate(req));
        assertTrue(ex.getMessage().contains("6 months"));
    }

    // TC-DP valid passthrough
    @Test
    @DisplayName("Chain passes valid request without throwing")
    void validate_validRequest_noException() {
        CreateBorrowRequestDTO req = new CreateBorrowRequestDTO(
                1L, 1, "purpose", LocalDate.now().plusDays(7));

        assertDoesNotThrow(() -> validatorChain.validate(req));
    }
}
