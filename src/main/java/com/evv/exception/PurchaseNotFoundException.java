package com.evv.exception;

/**
 * Пробрасывается в случае запроса несуществующей или не принадлежащей авторизованному в настоящий
 *  момент клиенту покупки.
 */
public class PurchaseNotFoundException extends RuntimeException{

    private static final String MESSAGE = "Purchase with id = %d not found for current user";

    public PurchaseNotFoundException(Long id) {
        super(MESSAGE.formatted(id));
    }
}
