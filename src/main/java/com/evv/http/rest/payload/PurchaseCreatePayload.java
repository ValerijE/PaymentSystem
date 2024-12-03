package com.evv.http.rest.payload;

import com.evv.dto.validation.PaymentChoiceHolder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Класс содержащий данные, необходимые для совершения покупки, в том числе способ оплаты:
 * кредитной картой или банковским счетом. Для выбранного способа оплаты должен быть указан
 * id (creditCardId или accountId), другой способ оплаты должен быть "null"
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Account id or Credit card id must be received and second one must be null")
public class PurchaseCreatePayload extends PaymentChoiceHolder {

    @NotNull(message = "{validation.NotNull.PurchaseCreatePayload.totalCost}")
    @PositiveOrZero(message = "{validation.PozitiveOrZero.PurchaseCreatePayload.totalCost}")
    BigDecimal totalCost;

    @NotEmpty(message = "{validation.NotEmpty.PurchaseCreatePayload.chosenProductIds}")
    Set<Long> chosenProductIds;

    public PurchaseCreatePayload(BigDecimal totalCost, Set<Long> chosenProductIds, Long creditCardId, Long accountId) {
        super(creditCardId, accountId);
        this.totalCost = totalCost;
        this.chosenProductIds = chosenProductIds;
    }
}
