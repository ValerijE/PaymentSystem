package com.evv.exception;

/**
 * Исключение пробрасывается при возникновении неизвестной ошибки в ходе попытки сохранения платежа с банковского счета.
 */
public class PaymentAccountCantBeSavedException extends RuntimeException{

    private static final String MESSAGE = "Payment from account id = %d can't be saved";

    public PaymentAccountCantBeSavedException(Long accountId) {
        super(MESSAGE.formatted(accountId));
    }
}
