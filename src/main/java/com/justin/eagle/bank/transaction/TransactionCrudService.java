package com.justin.eagle.bank.transaction;

import com.justin.eagle.bank.domain.ApprovedTransaction;
import com.justin.eagle.bank.domain.TransactionRequest;

public interface TransactionCrudService {

    ApprovedTransaction create(TransactionRequest request);
}
