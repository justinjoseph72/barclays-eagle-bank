package com.justin.eagle.bank.dao.model;

import java.util.UUID;

import lombok.Builder;

@Builder
public record AccountStatusInfo(UUID accountId, String accountNumber, String status) {
}
