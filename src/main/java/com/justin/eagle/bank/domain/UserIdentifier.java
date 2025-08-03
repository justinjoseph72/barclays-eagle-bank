package com.justin.eagle.bank.domain;

import java.util.UUID;

import lombok.Builder;

//TODO make Provisioned user extend this and use this in Active account
@Builder
public record UserIdentifier(UUID partyId, String externalUserId) {
}
