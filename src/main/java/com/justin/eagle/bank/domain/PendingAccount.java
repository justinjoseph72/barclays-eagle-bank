package com.justin.eagle.bank.domain;

import lombok.Builder;

@Builder
public record PendingAccount(String userId, String name, String type) implements Account {
}
