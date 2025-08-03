package com.justin.eagle.bank.domain;

public sealed interface ApprovedTransaction extends Transaction permits CreditTransaction, DebitTransaction {

    TransactionIdentifier transactionId();

    UserIdentifier userIdentifier();

    AccountIdentifier accountIdentifier();

    Amount transactionAmount();

    AuditData auditData();

}
