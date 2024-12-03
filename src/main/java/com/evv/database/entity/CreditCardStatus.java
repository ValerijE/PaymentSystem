package com.evv.database.entity;

import java.util.Arrays;
import java.util.Optional;

public enum CreditCardStatus {

    ACTIVE (1000),
    LIMIT_EXCEEDED (2000),
    BLOCKED (3000);

    private final int id;

    CreditCardStatus(Integer id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Optional<CreditCardStatus> find(String creditCardStatus) {
        return Arrays.stream(values())
                .filter(it -> it.name().equals(creditCardStatus))
                .findFirst();
    }
}
