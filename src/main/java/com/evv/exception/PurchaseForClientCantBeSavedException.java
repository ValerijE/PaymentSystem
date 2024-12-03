package com.evv.exception;

/**
 * Исключение пробрасывается при возникновении неизвестной ошибки в ходе попытки сохранения покупки клиента.
 */
public class PurchaseForClientCantBeSavedException extends RuntimeException{

    private static final String MESSAGE = "Purchase for client id = %d can't be saved";

    public PurchaseForClientCantBeSavedException(Long clientId) {
        super(MESSAGE.formatted(clientId));
    }
}
