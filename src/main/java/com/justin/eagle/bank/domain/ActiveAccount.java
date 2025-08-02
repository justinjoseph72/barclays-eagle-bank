package com.justin.eagle.bank.domain;

import java.util.UUID;

import lombok.Builder;

@Builder
public record ActiveAccount(UUID id, UUID partyId,
                            AccountIdentifier identifier,
                            Balance currentBalance,
                            AuditData auditData,
                            String name, String type) implements Account {
}
