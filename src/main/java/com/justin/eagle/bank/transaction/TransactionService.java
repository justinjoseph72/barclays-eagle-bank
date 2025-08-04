package com.justin.eagle.bank.transaction;

import java.util.List;

import com.justin.eagle.bank.domain.ApprovedTransaction;
import com.justin.eagle.bank.domain.TransactionRequest;

public interface TransactionService {

    ApprovedTransaction create(TransactionRequest request);

    List<ApprovedTransaction> fetchTransactions(String userId, String accountNumber);

    ApprovedTransaction fetchTransaction(String userId, String accountNumber, String transactionId);
}
