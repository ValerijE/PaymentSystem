package com.evv.exception;

/**
 * Исключение пробрасывается в ходе неудачной попытки снятия средств с банковского счета.
 */
public class UnableToChargeMoneyFromAccountException extends RuntimeException{

    private static final String MESSAGE = "Unable to charge money from account id = %d";

    public UnableToChargeMoneyFromAccountException(Long creditCardId) {
        super(MESSAGE.formatted(creditCardId));
    }
}
