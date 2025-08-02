package com.justin.eagle.bank.domain;

import lombok.Builder;

@Builder
public record AccountIdentifier(String accountNumber, String sortCode) {
}
