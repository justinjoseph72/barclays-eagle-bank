package com.justin.eagle.bank.dao.model;

import java.util.UUID;

import lombok.Builder;

@Builder
public record UserStatusDbInfo(UUID partyId, String userId, String status) {
}
