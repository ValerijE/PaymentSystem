package com.evv.exception;

/**
 * Пробрасывается в случае запроса несуществующего клиента по id.
 */
public class ClientByIdNotFoundException extends RuntimeException{

    private static final String MESSAGE = "Client with id = %d not found";

    public ClientByIdNotFoundException(Long clientId) {
        super(MESSAGE.formatted(clientId));
    }
}
