package com.evv.exception;

/**
 * Пробрасывается при попытке снятия средств с заблокированной кредитной карты.
 */
public class AttemptToChargeMoneyFromBlockedCreditCardException extends RuntimeException{

    private static final String MESSAGE = "There was an attempt to charge money from blocked credit card id = %d";

    public AttemptToChargeMoneyFromBlockedCreditCardException(Long creditCardId) {
        super(MESSAGE.formatted(creditCardId));
    }
}
