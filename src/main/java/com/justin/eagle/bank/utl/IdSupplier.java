package com.justin.eagle.bank.utl;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class IdSupplier {

    public UUID getNewId() {
        return UUID.randomUUID();
    }

    public String getNewUserExternalId() {
        return "%s-%s".formatted("usr", getNewId().toString().replace("-", ""));
    }
}
