package com.evv.exception;

/**
 * Исключение пробрасывается при возникновении неизвестной ошибки в ходе попытки сохранения платежа с кредитной карты.
 */
public class PaymentCreditCardCantBeSavedException extends RuntimeException{

    private static final String MESSAGE = "Payment from Credit Card id = %d can't be saved";

    public PaymentCreditCardCantBeSavedException(Long creditCardId) {
        super(MESSAGE.formatted(creditCardId));
    }
}
