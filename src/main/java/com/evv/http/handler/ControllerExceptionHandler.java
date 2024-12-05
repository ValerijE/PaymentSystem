package com.evv.http.handler;

import com.evv.dto.AccountDto;
import com.evv.dto.CustomUserDetails;
import com.evv.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Обработчик исключений при работе с формами Thymeleaf.
 * <p>Дополнительно предоставляет кастомную реализацию AccessDeniedHandler для обработки
 * AccessDeniedException, автоматически сформированного spring-security при попытке доступа
 * к методам Thymeleaf контроллеров без соответствующих прав.
 */
@Slf4j
@ControllerAdvice(basePackages = "com.evv.http.controller")
@RequiredArgsConstructor
public class ControllerExceptionHandler implements AccessDeniedHandler {

    private final ViewResolver viewResolver;

    @ExceptionHandler(AccountInsufficientFundsException.class)
    public String handleAccountInsufficientFundsException(AccountInsufficientFundsException e,
                                  @SessionAttribute("chosenAccount") AccountDto.Read.Public chosenAccount,
                                  @SessionAttribute("totalCost") BigDecimal totalCost,
                                  Model model) {
        log.debug("Insufficient funds in account id = %d with balance = %s for purchase total cost = %s"
                        .formatted(chosenAccount.getId(), chosenAccount.getBalance(), totalCost), e);

        BigDecimal lackFunds = totalCost.subtract(chosenAccount.getBalance());

        model
                .addAttribute("chosenAccount", chosenAccount)
                .addAttribute("totalCost", totalCost)
                .addAttribute("lackFunds", lackFunds);

        return "error/account_insufficient_funds";
    }

    @ExceptionHandler({
            // Исключения слоя контроллеров:
            FaultChoosingPaymentMethodException.class,
            PurchaseForClientCantBeSavedException.class,
            MethodArgumentNotValidException.class,
            // Исключения из прочих слоев:
            AccountNotFoundException.class,
            ClientByIdNotFoundException.class,
            ClientCantBeSaveException.class,
            CreditCardNotFoundException.class,
            PaymentAccountCantBeSavedException.class,
            PaymentCreditCardCantBeSavedException.class,
            PaymentNotFoundException.class,
            UnableToChargeMoneyFromAccountException.class,
            UnableToChargeMoneyFromCreditCardException.class
    })
    public String handleMainExceptions(Exception e,
                                  Model model) {
        log.debug("Caught by ControllerAdvice: {}", e.getMessage());

        if (e instanceof BindException ex) {
            model.addAttribute("message",
                    ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        } else {
            model.addAttribute("message", e.getMessage());
        }
        return "error/common_error_page";
    }

    @ExceptionHandler(Exception.class)
    public String handleOtherExceptions(Exception e,
                                  Model model) {
        log.warn("Caught by ControllerAdvice: {}", e.getMessage());

        model.addAttribute("message", "There was server error. Please make your request later.");

        return "error/common_error_page";
    }

    // Нижеследующий код направлен на обработку AccessDeniedException.

    /**
     * Обработчик AccessDeniedException.
     * Рендеринг возвращаемого значения производится в методе handle(..).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(HttpServletRequest request) {

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

        return "/error/access_denied";
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)  {

        ModelAndView modelAndView = new ModelAndView(handleAccessDeniedException(request));

        response.setStatus(HttpStatus.FORBIDDEN.value());

        // Далее вручную отправляем viewName на рендеринг и пользователь получает страницу с уведомлением о недостаточности прав.
        Locale locale = LocaleContextHolder.getLocale();
        try {
            String viewName = modelAndView.getViewName();
            View view = viewResolver.resolveViewName(viewName, locale);
            view.render(modelAndView.getModel(), request, response);
        } catch (Exception ex) {
            throw new FaultProcessAccessDeniedException(ex);
        }
    }
}
