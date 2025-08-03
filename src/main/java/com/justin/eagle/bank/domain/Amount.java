package com.justin.eagle.bank.domain;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record Amount(String currency, BigDecimal amount) {
}
