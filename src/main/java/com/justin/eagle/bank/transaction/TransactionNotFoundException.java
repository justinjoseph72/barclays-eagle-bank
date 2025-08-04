package com.justin.eagle.bank.transaction;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(String userId, String accountNumber, String transactionId) {
    }
}
