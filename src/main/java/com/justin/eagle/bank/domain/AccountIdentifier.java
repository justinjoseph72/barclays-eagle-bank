package com.justin.eagle.bank.domain;

import java.util.UUID;

import lombok.Builder;

@Builder
public record AccountIdentifier(UUID id, String accountNumber, String sortCode) {
}
