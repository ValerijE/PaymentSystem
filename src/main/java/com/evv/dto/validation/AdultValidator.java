package com.evv.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

import static com.evv.util.Util.ADULT_AGE;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    @Override
    public boolean isValid(LocalDate bd, ConstraintValidatorContext context) {
        if (bd == null) {
            return false;
        }
        LocalDate minAdultBirthDate = LocalDate.now().minusYears(ADULT_AGE);
        return bd.compareTo(minAdultBirthDate) <= 0;
    }
}
