package edu.cit.rentuma.techloan.config;

import edu.cit.rentuma.techloan.validator.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for setting up the Chain of Responsibility validator pattern.
 * Constructs the validation chain at application startup.
 */
@Configuration
public class ValidatorConfig {

    @Autowired
    private ItemNameValidator itemNameValidator;

    @Autowired
    private DueDateValidator dueDateValidator;

    @Autowired
    private DescriptionValidator descriptionValidator;

    /**
     * Constructs the validation chain:
     * ItemName → DueDate → Description
     *
     * @return the first validator in the chain
     */
    @Bean
    public BorrowRequestValidator borrowRequestValidatorChain() {
        itemNameValidator.setNext(dueDateValidator);
        dueDateValidator.setNext(descriptionValidator);
        return itemNameValidator;
    }
}
