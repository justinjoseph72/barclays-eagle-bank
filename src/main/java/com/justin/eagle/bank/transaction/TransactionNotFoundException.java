package com.justin.eagle.bank.transaction;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(String userId, String accountNumber, String transactionId) {
        super("transaction '%s' for user: '%s' and account '%s' not found ".formatted(transactionId, userId, accountNumber));
    }
}
