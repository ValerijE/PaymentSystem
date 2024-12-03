package com.evv.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для валидации класса в части наличия ровно одного идентификатора способа оплаты:
 * либо id кредитной карты, либо id банковского счета.
 */
@Constraint(validatedBy = {PaymentIdPresentValidator.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PaymentIdPresent {

    String message() default "{validation.PaymentIdPresent.default}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}