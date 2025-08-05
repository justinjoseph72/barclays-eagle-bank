package com.justin.eagle.bank.transaction;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.justin.eagle.bank.account.NoAccountFoundException;
import com.justin.eagle.bank.dao.TransactionRepository;
import com.justin.eagle.bank.dao.model.TransactionLog;
import com.justin.eagle.bank.dao.model.UserAccountBalanceInfo;
import com.justin.eagle.bank.domain.ApprovedTransaction;
import com.justin.eagle.bank.domain.TransactionRequest;
import com.justin.eagle.bank.rest.controller.UserNotFoundException;
import com.justin.eagle.bank.transaction.model.TransactionErrorCause;
import com.justin.eagle.bank.transaction.model.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ApprovedTransactionMapper approvedTransactionMapper;

    private final Map<TransactionErrorCause, BiPredicate<TransactionRequest, UserAccountBalanceInfo>> validationMap;

    public TransactionServiceImpl(TransactionRepository transactionRepository, ApprovedTransactionMapper approvedTransactionMapper) {
        this.transactionRepository = transactionRepository;
        this.approvedTransactionMapper = approvedTransactionMapper;
        this.validationMap = Map.of(
                TransactionErrorCause.ENTITIES_IN_INVAILD_STATE,
                (request, info) -> Stream.of(info.accountInfo().status(), info.userInfo().status())
                        .anyMatch(status -> !"ACTIVE".equals(status)), TransactionErrorCause.INVALID_CURRENCY,
                (request, info) -> !Objects.equals(info.currency(), request.amount().currency()), TransactionErrorCause.INSUFFICIENT_BALANCE,
                (request, info) -> TransactionType.DEBIT.equals(request.type()) && request.amount().amount().compareTo(info.currentBalance()) > 0);
    }

    @Override
    public ApprovedTransaction create(TransactionRequest request) {

        final UserAccountBalanceInfo info = getUserAccountBalanceInfo(request.userId(), request.accountNumber());

        performBusinessValidationForCreateTransaction(request, info);

        final ApprovedTransaction transaction = approvedTransactionMapper.build(request, info);
        transactionRepository.updateBalanceForTransaction(transaction);
        log.info("Successfully created {} transaction for user '{}' and account '{}' with  transaction id '{}", request.type(), transaction.userIdentifier().externalUserId(),
                transaction.accountIdentifier().accountNumber(), transaction.transactionId().externalId());
        return transaction;
    }

    @Override
    public List<ApprovedTransaction> fetchTransactions(String userId, String accountNumber) {
        final UserAccountBalanceInfo info = getUserAccountBalanceInfo(userId, accountNumber);

        var transactionInfos = transactionRepository.fetchAllTransactions(info.userInfo().partyId(), info.accountInfo().accountId());
        return transactionInfos.stream()
                .map(transactionLog -> approvedTransactionMapper.map(info, transactionLog))
                .toList();
    }

    @Override
    public ApprovedTransaction fetchTransaction(String userId, String accountNumber, String transactionId) {
        final UserAccountBalanceInfo info = getUserAccountBalanceInfo(userId, accountNumber);
        Optional<TransactionLog> transactionLog = transactionRepository.fetchTransaction(info.userInfo().partyId(), info.accountInfo().accountId(), transactionId);
        return transactionLog.map(log -> approvedTransactionMapper.map(info, log))
                .orElseThrow(() -> {
                    log.info("transaction id {} not found", transactionId);
                    return new TransactionNotFoundException(userId, accountNumber, transactionId);
                });
    }

    private UserAccountBalanceInfo getUserAccountBalanceInfo(String userId, String accountNumber) {
        final Optional<UserAccountBalanceInfo> userAccountBalanceInfo = transactionRepository.fetchLatestStatusForUserAndAccount(userId,
                accountNumber);
        return userAccountBalanceInfo
                .orElseThrow(() -> {
                    log.warn("no matching account found for userId '{}' and account number '{}'", userId, accountNumber);
                    return new NoAccountFoundException(accountNumber);
                });
    }

    private void performBusinessValidationForCreateTransaction(TransactionRequest request, UserAccountBalanceInfo info) {
        final Optional<TransactionErrorCause> failedTransaction = validationMap.entrySet()
                .stream()
                .takeWhile(entry -> entry.getValue().test(request, info))
                .map(Map.Entry::getKey)
                .findFirst();

        failedTransaction.ifPresent(errorCause -> {
            log.warn("the transaction failed with cause '{}'", errorCause);
            throw new FailedTransactionException(request, errorCause);
        });
    }

}
