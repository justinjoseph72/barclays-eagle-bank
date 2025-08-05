package com.justin.eagle.bank.rest.controller;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("User '%s' not found".formatted(userId));
    }
}
