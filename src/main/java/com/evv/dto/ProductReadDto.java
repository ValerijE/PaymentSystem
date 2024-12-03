package com.evv.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class ProductReadDto {

    Long id;

    String name;

    BigDecimal cost;
}
