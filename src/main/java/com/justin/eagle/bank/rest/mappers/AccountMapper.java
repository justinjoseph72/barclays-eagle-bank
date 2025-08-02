package com.justin.eagle.bank.rest.mappers;

import com.justin.eagle.bank.domain.ActiveAccount;
import com.justin.eagle.bank.domain.PendingAccount;
import com.justin.eagle.bank.generated.openapi.rest.model.BankAccountResponse;
import com.justin.eagle.bank.generated.openapi.rest.model.CreateBankAccountRequest;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public PendingAccount buildPendingAccount(String authorizedUserId, CreateBankAccountRequest createBankAccountRequest) {
        return PendingAccount.builder().userId(authorizedUserId)
                .name(createBankAccountRequest.getName())
                .type(createBankAccountRequest.getAccountType().getValue())
                .build();
    }

    public BankAccountResponse buildAccountResponse(ActiveAccount newAccount) {

        return BankAccountResponse.builder()
                .accountNumber(newAccount.identifier().accountNumber())
                .sortCode(BankAccountResponse.SortCodeEnum._10_10_10)
                .name(newAccount.name())
                .accountType(BankAccountResponse.AccountTypeEnum.fromValue(newAccount.type()))
                .createdTimestamp(newAccount.auditData().createdTimestamp())
                .updatedTimestamp(newAccount.auditData().lastUpdatedTimestamp())
                .balance(newAccount.currentBalance().amount().doubleValue())
                .currency(BankAccountResponse.CurrencyEnum.fromValue(newAccount.currentBalance().currency()))
                .build();
    }
}
