package com.justin.eagle.bank.domain;

import java.util.UUID;

import lombok.Builder;

@Builder
public record ActiveAccount( UUID partyId,
                            AccountIdentifier identifier,
                            Amount currentBalance,
                            AuditData auditData,
                            String name, String type) implements Account {
}
