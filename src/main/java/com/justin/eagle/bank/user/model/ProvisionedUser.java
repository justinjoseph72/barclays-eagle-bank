package com.justin.eagle.bank.user.model;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ProvisionedUser(UUID userId, String externalUserId, NewUser user, Instant createdTimestamp, Instant updatedTimestamp) implements User{
}
