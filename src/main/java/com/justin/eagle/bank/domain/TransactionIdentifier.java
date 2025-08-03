package com.justin.eagle.bank.domain;

import java.util.UUID;

import lombok.Builder;

@Builder
public record TransactionIdentifier(UUID id, String externalId, String reference) {
}
