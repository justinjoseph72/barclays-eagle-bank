package com.justin.eagle.bank.embedded;

public class EmbeddedServerRunner {
    static EmbeddedPostgresServer POSTGRES;

    public static void initialize() {
        initalizePostgres();
    }

    private static void initalizePostgres() {
        POSTGRES = EmbeddedPostgresServer.getInstance();
        POSTGRES.start();

        System.setProperty("db.url", POSTGRES.getJdbcUrl());
        System.setProperty("db.user", "postgres");
        System.setProperty("db.password", "postgres");
    }
}
