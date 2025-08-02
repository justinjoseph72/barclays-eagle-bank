package com.justin.eagle.bank.domain;

import lombok.Builder;

@Builder
public record  NewUser(
        UserProfile profile,
        UserAddress address
) implements User {
}
