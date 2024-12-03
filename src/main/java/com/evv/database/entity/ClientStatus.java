package com.evv.database.entity;

import java.util.Arrays;
import java.util.Optional;

public enum ClientStatus {
    ACTIVE,
    BLOCKED,
    NEED_TO_CHECK;

    public static Optional<ClientStatus> find(String clientStatus) {
        return Arrays.stream(values())
                .filter(it -> it.name().equals(clientStatus))
                .findFirst();
    }
}
