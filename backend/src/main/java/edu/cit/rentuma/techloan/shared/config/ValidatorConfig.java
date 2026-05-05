package edu.cit.rentuma.techloan.shared.config;

import edu.cit.rentuma.techloan.features.reservation.validator.BorrowRequestValidator;
import edu.cit.rentuma.techloan.features.reservation.validator.DescriptionValidator;
import edu.cit.rentuma.techloan.features.reservation.validator.DueDateValidator;
import edu.cit.rentuma.techloan.features.reservation.validator.ItemNameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidatorConfig {

    @Autowired
    private ItemNameValidator itemNameValidator;

    @Autowired
    private DueDateValidator dueDateValidator;

    @Autowired
    private DescriptionValidator descriptionValidator;

    @Bean
    public BorrowRequestValidator borrowRequestValidatorChain() {
        itemNameValidator.setNext(dueDateValidator);
        dueDateValidator.setNext(descriptionValidator);
        return itemNameValidator;
    }
}
