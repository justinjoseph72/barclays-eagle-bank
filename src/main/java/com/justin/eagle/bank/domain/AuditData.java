package com.justin.eagle.bank.domain;

import java.time.Instant;

import lombok.Builder;

@Builder
public record AuditData(Instant createdTimestamp, Instant lastUpdatedTimestamp) {
}
