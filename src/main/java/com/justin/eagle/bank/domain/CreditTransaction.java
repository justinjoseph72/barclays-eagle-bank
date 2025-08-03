package com.justin.eagle.bank.domain;

import lombok.Builder;

@Builder
public record CreditTransaction(UserIdentifier userIdentifier,
                                AccountIdentifier accountIdentifier,
                                Amount transactionAmount,
                                TransactionIdentifier transactionId,
                                AuditData auditData) implements ApprovedTransaction {

}
