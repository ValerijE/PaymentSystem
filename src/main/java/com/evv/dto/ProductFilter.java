package com.evv.dto;

import java.math.BigDecimal;

public record ProductFilter (String name, BigDecimal minCost, BigDecimal maxCost) { }
