package com.evv.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id", callSuper = false)
public abstract class PaymentReadDto {

    Long id;

    BigDecimal amount;

}
