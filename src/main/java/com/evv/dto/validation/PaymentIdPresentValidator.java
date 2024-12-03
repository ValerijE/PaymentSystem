package com.evv.dto.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PaymentIdPresentValidator implements
        ConstraintValidator<PaymentIdPresent, PaymentChoiceHolder> {

    @Override
    public boolean isValid(PaymentChoiceHolder value, ConstraintValidatorContext context) {
        boolean isCreditCardPresent = value.getCreditCardId() != null;
        boolean isAccountPresent = value.getAccountId() != null;
        return isCreditCardPresent ^ isAccountPresent;
    }
}
