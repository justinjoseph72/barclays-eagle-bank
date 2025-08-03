package com.justin.eagle.bank.transaction;

import com.justin.eagle.bank.domain.TransactionRequest;
import com.justin.eagle.bank.transaction.model.TransactionErrorCause;

//TODO build 422 error or 401 based on error cause
public class FailedTransactionException extends RuntimeException {

    public FailedTransactionException(TransactionRequest request, TransactionErrorCause errorCause) {

    }
}
