package com.evv.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.Set;

@Value
public class PurchaseCreateEditDto {

    @NotNull(message = "{validation.NotNull.PurchaseCreateEditDto.client}")
    ClientReadDto client;

    @NotEmpty(message = "{validation.NotEmpty.PurchaseCreateEditDto.client}")
    Set<ProductReadDto> products;

    @NotNull(message = "{validation.NotNull.PurchaseCreateEditDto.paymentId}")
    Long paymentId;
}
