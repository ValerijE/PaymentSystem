package com.evv.exception;

/**
 * Исключение пробрасывается в ходе запроса несуществующего платежа.
 */
public class PaymentNotFoundException extends RuntimeException{

    private static final String MESSAGE = "Payment with id = %d not found";

    public PaymentNotFoundException(Long id) {
        super(MESSAGE.formatted(id));
    }
}
