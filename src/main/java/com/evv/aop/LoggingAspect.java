package com.evv.aop;

import com.evv.dto.CustomUserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Класс осуществляющий логирование public-методов сервисного слоя.
 * Принадлежность класса сервисному слою определяется по аннотации @Service на уровне класса.
 * Логируются события: вызов метода, успешный возврат результата.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void anyPublicServiceMethod() {
    }

    @Before("anyPublicServiceMethod()" +
            "&& target(service)")
    public void addLoggingBefore(JoinPoint joinPoint,
                                 Object service) {
        String methodName = joinPoint.getSignature().getName();
        Object[] argsArray = joinPoint.getArgs();
        log.info("Public service layer method \"{}\" with parameter(s) {} of class {} was invoked",
                methodName, argsArray, service);
    }

    @AfterReturning(value = "anyPublicServiceMethod()" +
            "&& target(service)",
            returning = "result")
    public void addLoggingAfter(JoinPoint joinPoint,
                                Object service, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String resultMessage;
        if (result instanceof CustomUserDetailsImpl userDetails) { //  Чтобы не логировать зашифрованный пароль для метода loadUserByUsername возвращающего CustomUserDetailsImpl.
            resultMessage = "It was user with name - %s. All other fields were hidden for security reason"
                    .formatted(userDetails.getUsername());
        } else if (result != null) {
            resultMessage = result.toString();
        } else {
            resultMessage = "null";
        }
        log.info("Public service layer method \"{}\" with result \"{}\" of class {} was completed",
                methodName, resultMessage, service);
    }
}
