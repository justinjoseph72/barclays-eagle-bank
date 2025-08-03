package com.justin.eagle.bank.account;

public class NoAccountFoundException extends RuntimeException {

    public NoAccountFoundException(String accountNumber) {
        super("No account found for account number %s".formatted(accountNumber));
    }
}
