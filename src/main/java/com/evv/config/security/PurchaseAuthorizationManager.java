package com.evv.config.security;

import com.evv.dto.PurchaseReadDto;
import com.evv.dto.CustomUserDetails;
import com.evv.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Бин для проверки прав доступа по пути GET "/api/v1/purchases/{purchaseId:\\d+}".
 * <p>Доступ предоставляется пользователям с ролью CLIENT если производится запрос на получение собственных данных,
 * а также пользователям с ролью ADMIN.
 */
@Component
@RequiredArgsConstructor
public class PurchaseAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final PurchaseService purchaseService;

    @Override
    public AuthorizationDecision check(Supplier authenticationSupplier, RequestAuthorizationContext ctx) {

        Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principalObj instanceof CustomUserDetails principal) {
            if (principal.getAdmin() != null) {
                return new AuthorizationDecision(true);
            } else {
                Long clientId = principal.getClient().getId();

                Long purchaseId = Long.parseLong(ctx.getVariables().get("purchaseId"));

                Optional<PurchaseReadDto> maybePurchase = purchaseService.findById(purchaseId);
                if (maybePurchase.isEmpty()) { // Если не найдено, значит должны вернуть доступ разрешен и в контроллере проброситься 404
                    return new AuthorizationDecision(true);
                } else { // Если найдено, то смотрим принадлежит ли текущем клиенту. Нужно реализовать без бросания исключений.
                    Optional<Long> wrappedPurchaseClientId = maybePurchase
                            .map(purchaseReadDto -> purchaseReadDto.getClient().getId())
                            .filter(purchaseClientId -> Objects.equals(purchaseClientId, clientId));
                    return wrappedPurchaseClientId.isPresent()
                            ? new AuthorizationDecision(true)
                            : new AuthorizationDecision(false);
                }
            }
        } else {
            return new AuthorizationDecision(false);
        }

    }
}
