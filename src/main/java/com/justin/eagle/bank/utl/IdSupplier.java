package com.justin.eagle.bank.utl;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class IdSupplier {

    public UUID getNewId() {
        return UUID.randomUUID();
    }

    public String getNewUserExternalId() {
        return "%s-%s".formatted("usr", getNewId().toString().replace("-", ""));
    }

    public String getNewAccountNumber(@NotNull Integer value) {
        final String accountSuffix = StringUtils.leftPad(value.toString(), 6, "0");
        return "01" + accountSuffix;
    }
}
