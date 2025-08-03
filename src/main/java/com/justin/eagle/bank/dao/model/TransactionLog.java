package com.justin.eagle.bank.dao.model;

import java.util.UUID;

import com.justin.eagle.bank.domain.Amount;
import com.justin.eagle.bank.domain.AuditData;
import lombok.Builder;

@Builder
public record TransactionLog(UUID id, String transactionId, UUID partyId, UUID accountId, boolean isCredit, Amount amount, AuditData auditData ) {
}
