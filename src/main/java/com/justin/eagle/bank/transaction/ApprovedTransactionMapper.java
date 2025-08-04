package com.justin.eagle.bank.transaction;

import java.util.List;

import com.justin.eagle.bank.dao.model.TransactionLog;
import com.justin.eagle.bank.dao.model.UserAccountBalanceInfo;
import com.justin.eagle.bank.domain.AccountIdentifier;
import com.justin.eagle.bank.domain.ApprovedTransaction;
import com.justin.eagle.bank.domain.AuditData;
import com.justin.eagle.bank.domain.CreditTransaction;
import com.justin.eagle.bank.domain.DebitTransaction;
import com.justin.eagle.bank.domain.TransactionIdentifier;
import com.justin.eagle.bank.domain.TransactionRequest;
import com.justin.eagle.bank.domain.UserIdentifier;
import com.justin.eagle.bank.utl.IdSupplier;
import com.justin.eagle.bank.utl.NowTimeSupplier;
import org.springframework.stereotype.Component;

@Component
class ApprovedTransactionMapper {

    private final IdSupplier idSupplier;

    private final NowTimeSupplier nowTimeSupplier;

    ApprovedTransactionMapper(IdSupplier idSupplier, NowTimeSupplier nowTimeSupplier) {
        this.idSupplier = idSupplier;
        this.nowTimeSupplier = nowTimeSupplier;
    }

    public ApprovedTransaction build(TransactionRequest request, UserAccountBalanceInfo info) {

        return switch (request.type()) {
            case CREDIT -> buildCreditTransaction(request, info);
            case DEBIT -> buildDebitTransaction(request, info);
        };
    }

    public ApprovedTransaction map(UserAccountBalanceInfo accountBalanceInfo, TransactionLog transactionLog) {
        return transactionLog.isCredit() ? buildCreditTransaction(accountBalanceInfo, transactionLog) :
                buildDebitTransaction(accountBalanceInfo, transactionLog);
    }

    private ApprovedTransaction buildCreditTransaction(UserAccountBalanceInfo info, TransactionLog log) {
        return CreditTransaction.builder()
                .transactionId(buildTransactionIdentifier(log))
                .transactionAmount(log.amount())
                .auditData(buildAuditData(log))
                .userIdentifier(buildUserIdentifier(info))
                .accountIdentifier(buildAccountIdentifier(info))
                .build();
    }

    private ApprovedTransaction buildDebitTransaction(UserAccountBalanceInfo info, TransactionLog log) {
        return DebitTransaction.builder()
                .transactionId(buildTransactionIdentifier(log))
                .transactionAmount(log.amount())
                .auditData(buildAuditData(log))
                .userIdentifier(buildUserIdentifier(info))
                .accountIdentifier(buildAccountIdentifier(info))
                .build();
    }


    private ApprovedTransaction buildDebitTransaction(TransactionRequest request, UserAccountBalanceInfo info) {
        return DebitTransaction.builder()
                .transactionId(buildTransactionIdentifier(request))
                .transactionAmount(request.amount())
                .auditData(buildAuditData())
                .userIdentifier(buildUserIdentifier(info))
                .accountIdentifier(buildAccountIdentifier(info))
                .build();
    }

    private ApprovedTransaction buildCreditTransaction(TransactionRequest request, UserAccountBalanceInfo info) {
        return CreditTransaction.builder()
                .transactionId(buildTransactionIdentifier(request))
                .transactionAmount(request.amount())
                .auditData(buildAuditData())
                .userIdentifier(buildUserIdentifier(info))
                .accountIdentifier(buildAccountIdentifier(info))
                .build();
    }

    private static AccountIdentifier buildAccountIdentifier(UserAccountBalanceInfo info) {
        return AccountIdentifier.builder()
                .id(info.accountInfo().accountId())
                .accountNumber(info.accountInfo().accountNumber())
                .build();
    }

    private static UserIdentifier buildUserIdentifier(UserAccountBalanceInfo info) {
        return UserIdentifier.builder()
                .partyId(info.userInfo().partyId())
                .externalUserId(info.userInfo().userId())
                .build();
    }

    private AuditData buildAuditData() {
        return AuditData.builder()
                .createdTimestamp(nowTimeSupplier.currentInstant())
                .build();
    }

    private AuditData buildAuditData(TransactionLog log) {
        return AuditData.builder()
                .createdTimestamp(log.auditData().createdTimestamp())
                .build();
    }

    private TransactionIdentifier buildTransactionIdentifier(TransactionRequest request) {
        return TransactionIdentifier.builder()
                .id(idSupplier.getNewId())
                .externalId(idSupplier.newTransactionId())
                .reference(request.reference())
                .build();
    }

    private TransactionIdentifier buildTransactionIdentifier(TransactionLog log) {
        return TransactionIdentifier.builder()
                .id(log.id())
                .externalId(log.transactionId())
                .reference(log.reference())
                .build();
    }

}
