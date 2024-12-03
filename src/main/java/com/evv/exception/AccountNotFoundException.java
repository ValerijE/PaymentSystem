package com.evv.exception;

/**
 * Пробрасывается в случае запроса несуществующего или не принадлежащего авторизованному в настоящий
 * момент клиенту счета.
 */
public class AccountNotFoundException extends RuntimeException{

    private static final String MESSAGE = "Account with id = %d not found";

    public AccountNotFoundException(Long id) {
        super(MESSAGE.formatted(id));
    }
}
