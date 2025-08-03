package com.justin.eagle.bank.transaction.model;

import lombok.Getter;

@Getter
public enum TransactionType {
    CREDIT(true, "deposit"),
    DEBIT(false, "withdraw");
    private final boolean creditIndicator;
    private final String value;

    TransactionType(boolean creditIndicator, String value) {
        this.creditIndicator = creditIndicator;
        this.value = value;
    }

    TransactionType fromValue(String input) {
        for (TransactionType transactionType : TransactionType.values()) {
            if (transactionType.value.equalsIgnoreCase(input)) {
                return transactionType;
            }
        }
        throw new IllegalStateException("provided input not a valid value");
    }

}
