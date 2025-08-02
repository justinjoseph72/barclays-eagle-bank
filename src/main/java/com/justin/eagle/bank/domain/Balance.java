package com.justin.eagle.bank.domain;

import java.math.BigDecimal;

public record Balance(String currency, BigDecimal amount) {
}
