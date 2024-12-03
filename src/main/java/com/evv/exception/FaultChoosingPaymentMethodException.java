package com.evv.exception;

/**
 * Исключение, пробрасывается если выбранная пользователем кредитная карта или счет
 * отсутствует в списке кредитных карт или счетов, хранимых в сессии для данного пользователя.
 */
public class FaultChoosingPaymentMethodException extends RuntimeException{

    private static final String MESSAGE = "Error choosing payment method for client id = %d";

    public FaultChoosingPaymentMethodException(Long id) {
        super(MESSAGE.formatted(id));
    }
}
