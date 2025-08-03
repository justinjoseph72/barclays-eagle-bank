package com.justin.eagle.bank.account;

public class AccountViewForbiddenException extends RuntimeException {

    public AccountViewForbiddenException(String userId, String accountNumber) {
        super("the user %s is forbidden to view details of account %s".formatted(userId, accountNumber));
    }
}
