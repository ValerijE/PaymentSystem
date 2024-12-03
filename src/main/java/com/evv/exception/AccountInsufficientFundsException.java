package com.evv.exception;

/**
 * Пробрасывается при попытке проведения оплаты с банковского счета в случае обнаружения недостатка средств
 * на банковском счете.
 */
public class AccountInsufficientFundsException extends RuntimeException{

    private static final String MESSAGE = "Insufficient funds in account id = %d";

    public AccountInsufficientFundsException(Long accountId) {
        super(MESSAGE.formatted(accountId));
    }
}
