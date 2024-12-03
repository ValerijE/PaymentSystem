package com.evv.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class ProductCreateEditDto {

    @NotBlank(message = "{validation.NotNull.ProductCreateEditDto.name}")
    String name;

    @NotNull(message = "{validation.NotNull.ProductCreateEditDto.cost}")
    BigDecimal cost;
}
