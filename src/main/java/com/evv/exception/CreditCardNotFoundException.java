package com.evv.exception;

/**
 * Исключение пробрасывается в случае запроса несуществующей или не принадлежащей авторизованному в настоящий
 * момент клиенту кредитной карты.
 */
public class CreditCardNotFoundException extends RuntimeException{

    private static final String MESSAGE = "Credit card with id = %d not found";

    public CreditCardNotFoundException(Long id) {
        super(MESSAGE.formatted(id));
    }
}
