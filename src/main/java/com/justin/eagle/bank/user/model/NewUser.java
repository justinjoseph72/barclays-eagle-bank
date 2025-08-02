package com.justin.eagle.bank.user.model;

import java.util.UUID;

import lombok.Builder;

@Builder
public record  NewUser(
        UserProfile profile,
        UserAddress address
) implements User {
}
