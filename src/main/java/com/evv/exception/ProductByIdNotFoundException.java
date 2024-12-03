package com.evv.exception;

/**
 * Пробрасывается в случае запроса несуществующего продукта по id.
 */
public class ProductByIdNotFoundException extends RuntimeException{

    private static final String MESSAGE = "Product with id = %d not found";

    public ProductByIdNotFoundException(Long clientId) {
        super(MESSAGE.formatted(clientId));
    }
}
