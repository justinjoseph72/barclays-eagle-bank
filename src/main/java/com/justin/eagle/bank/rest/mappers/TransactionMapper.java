package com.justin.eagle.bank.rest.mappers;

import java.math.BigDecimal;
import java.util.List;

import com.justin.eagle.bank.domain.Amount;
import com.justin.eagle.bank.domain.ApprovedTransaction;
import com.justin.eagle.bank.domain.CreditTransaction;
import com.justin.eagle.bank.domain.DebitTransaction;
import com.justin.eagle.bank.domain.TransactionRequest;
import com.justin.eagle.bank.generated.openapi.rest.model.CreateTransactionRequest;
import com.justin.eagle.bank.generated.openapi.rest.model.ListTransactionsResponse;
import com.justin.eagle.bank.generated.openapi.rest.model.TransactionResponse;
import com.justin.eagle.bank.transaction.model.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionRequest buildTransactionRequest(String authorizedUserId,
            @Pattern(regexp = "^01\\d{6}$") String accountNumber,
            @Valid CreateTransactionRequest createTransactionRequest) {

        return TransactionRequest.builder()
                .type(TransactionType.valueOf(createTransactionRequest.getType().getValue()))
                .reference(createTransactionRequest.getReference())
                .userId(authorizedUserId)
                .accountNumber(accountNumber)
                .amount(Amount.builder()
                        .currency(createTransactionRequest.getCurrency().getValue())
                        .amount(BigDecimal.valueOf(createTransactionRequest.getAmount()))
                        .build())
                .build();

    }

    public TransactionResponse buildTransactionResponse(ApprovedTransaction transaction) {

        TransactionResponse.TypeEnum type = switch (transaction) {
            case CreditTransaction creditTransaction -> TransactionResponse.TypeEnum.DEPOSIT;
            case DebitTransaction debitTransaction -> TransactionResponse.TypeEnum.WITHDRAWAL;
        };

        return TransactionResponse.builder()
                .id(transaction.transactionId().externalId())
                .userId(transaction.userIdentifier().externalUserId())
                .reference(transaction.transactionId().reference())
                .type(type)
                .amount(transaction.transactionAmount().amount().doubleValue())
                .currency(TransactionResponse.CurrencyEnum.fromValue(transaction.transactionAmount().currency()))
                .createdTimestamp(transaction.auditData().createdTimestamp())
                .build();
    }

    public ListTransactionsResponse buildListTransactionsResponse(List<ApprovedTransaction> approvedTransactions) {
        return ListTransactionsResponse
                .builder()
                .transactions(approvedTransactions
                        .stream()
                        .map(this::buildTransactionResponse)
                        .toList())
                .build();
    }
}
