package com.evv.database.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter
public class CreditCardStatusConverter implements AttributeConverter<CreditCardStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(CreditCardStatus status) {
        return status.getId();
    }

    @Override
    public CreditCardStatus convertToEntityAttribute(Integer dbId) {
        return Arrays.stream(CreditCardStatus.values())
                .filter(ccs -> ccs.getId() == dbId)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
