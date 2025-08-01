package com.justin.eagle.bank.utl;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Component;

@Component
public class NowTimeSupplier {

    private final Clock clock;

    private static final String TIME_ZONE = "UTC";

    public NowTimeSupplier(Clock clock) {
        this.clock = clock;
    }

    public Long currentEpochSec() {
        return Instant.now(clock).getEpochSecond();
    }

    public Instant currentInstant() {
        return Instant.now(clock);
    }
}
