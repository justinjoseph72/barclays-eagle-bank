package com.justin.eagle.bank.dao;

public class DatabaseInteractionException extends RuntimeException {

    public DatabaseInteractionException(Exception e) {
        super(e);
    }
}
