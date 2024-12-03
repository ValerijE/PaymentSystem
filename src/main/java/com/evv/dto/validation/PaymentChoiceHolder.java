package com.evv.dto.validation;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@PaymentIdPresent
public class PaymentChoiceHolder {
    private final Long creditCardId;
    private final Long accountId;
}