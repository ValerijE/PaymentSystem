package com.evv.exception;

/**
 * Исключение пробрасывается в при возникновении любой ошибки в ходе кастомной обработки AccessDeniedException.
 */
public class FaultProcessAccessDeniedException extends RuntimeException{

    private static final String MESSAGE = "Some error occur while custom processing AccessDeniedException";

    public FaultProcessAccessDeniedException(Throwable e) {
        super(MESSAGE, e);
    }
}
