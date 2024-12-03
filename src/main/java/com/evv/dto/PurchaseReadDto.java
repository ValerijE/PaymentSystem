package com.evv.dto;

import lombok.Value;

import java.util.Set;

@Value
public class PurchaseReadDto {

    Long id;

    ClientReadDto client;

    Set<ProductReadDto> products;

    PaymentReadDto payment;
}
