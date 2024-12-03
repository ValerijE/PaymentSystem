package com.evv.dto;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.math.BigDecimal;

@Value
@EqualsAndHashCode(callSuper = true)
public class PaymentAccountReadDto extends PaymentReadDto {

    AccountDto.Read.Public account;

    public PaymentAccountReadDto(Long id, BigDecimal amount, AccountDto.Read.Public account) {
        super(id, amount);
        this.account = account;
    }
}
