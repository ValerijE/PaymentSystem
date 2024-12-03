package com.evv.exception;

/**
 * Исключение пробрасывается в ходе неудачной попытки снятия средств с кредитной карты.
 */
public class UnableToChargeMoneyFromCreditCardException extends RuntimeException{

    private static final String MESSAGE = "Unable to charge money from credit card id = %d";

    public UnableToChargeMoneyFromCreditCardException(Long creditCardId) {
        super(MESSAGE.formatted(creditCardId));
    }
}
