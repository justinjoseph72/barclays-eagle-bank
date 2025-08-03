package com.justin.eagle.bank.domain;

import lombok.Builder;

@Builder
public record UserInfo(
        UserProfile profile,
        UserAddress address
) implements User {
}
