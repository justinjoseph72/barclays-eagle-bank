package com.justin.eagle.bank.domain;

import com.justin.eagle.bank.transaction.model.TransactionType;
import lombok.Builder;

@Builder
public record TransactionRequest(String userId, String accountNumber, String reference, TransactionType type, Amount amount)  {
}
