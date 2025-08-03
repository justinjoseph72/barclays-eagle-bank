package com.justin.eagle.bank.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.justin.eagle.bank.dao.AccountRepository;
import com.justin.eagle.bank.domain.AccountIdentifier;
import com.justin.eagle.bank.domain.ActiveAccount;
import com.justin.eagle.bank.domain.AuditData;
import com.justin.eagle.bank.domain.Balance;
import com.justin.eagle.bank.domain.PendingAccount;
import com.justin.eagle.bank.dao.UserRepository;
import com.justin.eagle.bank.dao.model.UserStatusDbInfo;
import com.justin.eagle.bank.rest.controller.UserNotFoundException;
import com.justin.eagle.bank.utl.IdSupplier;
import com.justin.eagle.bank.utl.NowTimeSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class AccountCrudServiceImpl implements AccountCrudService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final IdSupplier idSupplier;
    private final NowTimeSupplier nowTimeSupplier;

    AccountCrudServiceImpl(UserRepository userRepository, AccountRepository accountRepository, IdSupplier idSupplier, NowTimeSupplier nowTimeSupplier) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.idSupplier = idSupplier;
        this.nowTimeSupplier = nowTimeSupplier;
    }

    @Override
    public ActiveAccount createNewAccount(PendingAccount accountDetails) {
        final UUID partyId = getPartyIdForActiveUserId(accountDetails.userId());

        final String newAccountNumber = accountRepository.getNewAccountNumber();
        Instant now = nowTimeSupplier.currentInstant();
        final ActiveAccount activeAccount = ActiveAccount
                .builder()
                .id(idSupplier.getNewId())
                .partyId(partyId)
                .name(accountDetails.name())
                .type(accountDetails.type())
                .identifier(AccountIdentifier.builder()
                        .accountNumber(newAccountNumber)
                        .sortCode("10-10-10")
                        .build())
                .currentBalance(Balance.builder()
                        .currency("GBP")
                        .amount(BigDecimal.ZERO)
                        .build())
                .auditData(AuditData.builder()
                        .createdTimestamp(now)
                        .lastUpdatedTimestamp(now)
                        .build())
                .build();
        accountRepository.persistNewAccount(activeAccount);

        return activeAccount;
    }

    @Override
    public List<ActiveAccount> fetchAllAccountsForUser(String userId) {
        final UUID partyId = getPartyIdForActiveUserId(userId);
        return accountRepository.findAllAccountsForUser(partyId);
    }

    @Override
    public ActiveAccount fetchAccountDetails(String accountNumber, String userId) {
        final UUID partyId = getPartyIdForActiveUserId(userId);
        final Optional<ActiveAccount> accountDetail = accountRepository.findAccountDetail(accountNumber);
        accountDetail
                .ifPresent(activeAccount -> {
                    if (!partyId.equals(activeAccount.partyId())) {
                        log.warn("user '{}' is forbidden to view account details of account'{}'", userId, accountNumber);
                        throw new AccountViewForbiddenException(userId, accountNumber);
                    }
                });
        return accountDetail.orElseThrow(() -> {
            log.warn("No details found for account number '{}'", accountNumber);
            return new NoAccountFoundException(accountNumber);
        });
    }

    private UUID getPartyIdForActiveUserId(String userId) {
        return userRepository.getUserStatusInfo(userId)
                .filter(user -> {
                    final boolean isActive = "ACTIVE".equals(user.status());
                    if (!isActive) {
                        log.warn("the user with id '{}' status is {}", user.userId(), user.status());
                    }
                    return isActive;
                })
                .map(UserStatusDbInfo::partyId)
                .orElseThrow(() -> {
                    log.warn("user with id '{}' not found", userId);
                    return new UserNotFoundException();
                });
    }
}
