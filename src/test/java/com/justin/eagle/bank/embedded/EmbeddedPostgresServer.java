package com.justin.eagle.bank.embedded;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import io.zonky.test.db.postgres.embedded.DefaultPostgresBinaryResolver;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

public class EmbeddedPostgresServer {

    private static EmbeddedPostgresServer WRAPPER;
    private static EmbeddedPostgres POSTGRES;

    private EmbeddedPostgresServer() {
        addShutdownHook();
    }

    public static EmbeddedPostgresServer getInstance() {
        if (WRAPPER == null) {
            WRAPPER = new EmbeddedPostgresServer();
        }
        return WRAPPER;
    }

    public void start() {
        try {
            POSTGRES = EmbeddedPostgres.builder()
                    .setPgBinaryResolver((system, machineHardware) -> {
                        var arch = system.equals("Darwin") && machineHardware.equals("aarch64") ? "x84_64" : machineHardware;
                        return DefaultPostgresBinaryResolver.INSTANCE.getPgBinary(system, arch);
                    })
                    .setOverrideWorkingDirectory(new File("."))
                    .start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getJdbcUrl() {
        Objects.requireNonNull(POSTGRES, "postgress required");
        return POSTGRES.getJdbcUrl("postgres", "postgres");
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Objects.requireNonNull(POSTGRES, "postgress is required");
                POSTGRES.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }));
    }
}
