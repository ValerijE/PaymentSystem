package com.evv.http.handler;

import com.evv.dto.CustomUserDetails;
import com.evv.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

/**
 * Обработчик исключений REST контроллеров.
 * <p>Дополнительно представляет кастомную реализацию AccessDeniedHandler
 * для обработки AccessDeniedException, автоматически сформированного
 * spring-security при попытке доступа к методам REST контроллеров без соответствующих прав.
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.evv.http.rest")
@RequiredArgsConstructor
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler implements AccessDeniedHandler,
        AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(AccountInsufficientFundsException.class)
    public ResponseEntity<ProblemDetail> handleAccountInsufficientFundsException(AccountInsufficientFundsException e) {

        log.debug(e.getMessage(), e);
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler({
            FaultChoosingPaymentMethodException.class,
            UnableToChargeMoneyFromAccountException.class,
            UnableToChargeMoneyFromCreditCardException.class
    })
    public ResponseEntity<ProblemDetail> handleMainExceptions(Exception e) {
        log.debug("Caught by ControllerAdvice: {}", e.getMessage());
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler({
            PurchaseForClientCantBeSavedException.class,
            ClientCantBeSaveException.class,
            PaymentAccountCantBeSavedException.class,
            PaymentCreditCardCantBeSavedException.class,
    })
    public ResponseEntity<ProblemDetail> handleCantBeSavedExceptions(Exception e) {
        log.debug("Caught by ControllerAdvice: {}", e.getMessage());
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.CONFLICT, e.getMessage(), "Can't save exception");
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler({
            AccountNotFoundException.class,
            ClientByIdNotFoundException.class,
            CreditCardNotFoundException.class,
            PaymentNotFoundException.class,
            ProductByIdNotFoundException.class,
            PurchaseNotFoundException.class
    })
    public ResponseEntity<ProblemDetail> handleNotFoundExceptions(Exception e) {
        log.debug("Caught by ControllerAdvice: {}", e.getMessage());
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.NOT_FOUND, e.getMessage(), "Not found exception");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleOtherExceptions(Exception e) {
        log.warn("Caught by ControllerAdvice: {}", e.getMessage());
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.debug(ex.getMessage(), ex);
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(),
                "Validation exception. For more information see the field 'errors'");

        problemDetail.setProperty("errors",
                ex.getAllErrors().stream()
                        .map(ObjectError::getDefaultMessage)
                        .toList());

        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String detail, String title) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setProperty("timestamp", Instant.now());
        if (title != null) {
            problemDetail.setTitle(title);
        } else {
            problemDetail.setTitle(detail);
        }
        return problemDetail;
    }

// Нижеследующий код направлен на обработку AccessDeniedException.

    /**
     * Формирует ResponseEntity с описанием AccessDeniedException.
     * <p> Автоматически вызывается в случае самостоятельного пробрасывания AccessDeniedException из REST
     * контроллеров.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(HttpServletRequest request) {

        String deniedRequestUri = request.getRequestURI();
        String method = request.getMethod();

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String clientId = null;
        if (principal instanceof CustomUserDetails cud) {
            clientId = cud.getClient().getId().toString();
        } else if (principal instanceof String strPrinc) {
            clientId = strPrinc;
        }

        String message = "Client id = %s attempted to access endpoint %s %s without appropriate permissions"
                .formatted(clientId, method, deniedRequestUri);
        log.debug(message);

        ProblemDetail problemDetail = createProblemDetail(HttpStatus.FORBIDDEN, message, "Access denied");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(problemDetail);
    }

    /**
     * Подготавливает поля объекта HttpServletResponse и отправляет клиенту ProblemDetail с описанием
     * AccessDeniedException и установленным полем instance.
     * <p>Метод вызывается автоматически после регистрации в SecurityFilterChain.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) {
        ResponseEntity<ProblemDetail> result = handleAccessDeniedException(request);

        response.setStatus(HttpStatus.FORBIDDEN.value());

        renderProblemDetail(request, response, result);
    }


// Нижеследующий код направлен на обработку AuthenticationException.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(HttpServletRequest request) {

        String deniedRequestUri = request.getRequestURI();
        String method = request.getMethod();

        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        String message = "Anonymous client ip = %s attempted to access endpoint %s %s without authentication"
                .formatted(ipAddress, method, deniedRequestUri);
        log.debug(message);

        ProblemDetail problemDetail = createProblemDetail(HttpStatus.UNAUTHORIZED, message, "Unauthorized");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(problemDetail);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        ResponseEntity<ProblemDetail> result = handleAuthenticationException(request);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        renderProblemDetail(request, response, result);
    }

    private void renderProblemDetail(HttpServletRequest request, HttpServletResponse response, ResponseEntity<ProblemDetail> result) {
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON.toString());

        String requestURI = request.getRequestURI();

        try {
            Objects.requireNonNull(result.getBody()).setInstance(new URI(requestURI)); // Установка поля instance
            objectMapper.writeValue(response.getOutputStream(), result.getBody());
        } catch (Exception ex) {
            throw new FaultProcessAccessDeniedException(ex);
        }
    }
}
