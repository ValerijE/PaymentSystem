package com.evv.exception;

/**
 * Исключение пробрасывается при невозможности сохранения нового клиента в БД
 * Наиболее вероятная причина - попытка сохранить нового клиента с уже существующим email.
 * Все остальные возможные ошибки при создании нового пользователя валидируются аннотациями полей
 * классов ClientCreateEditDto и UserCreateEditDto.
 */
public class ClientCantBeSaveException extends RuntimeException{

    private static final String MESSAGE = "The error occurred while trying to save client. Possible you trying to register client with existing email \"%s\", or there are some other database problems";

    public ClientCantBeSaveException(String email) {
        super(MESSAGE.formatted(email));
    }
}
