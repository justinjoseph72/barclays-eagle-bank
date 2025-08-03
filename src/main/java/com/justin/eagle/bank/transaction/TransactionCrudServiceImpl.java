package com.justin.eagle.bank.transaction;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.justin.eagle.bank.dao.TransactionRepository;
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
class TransactionCrudServiceImpl implements TransactionCrudService {

    private final TransactionRepository transactionRepository;
    private final ApprovedTransactionMapper approvedTransactionMapper;

    private final Map<TransactionErrorCause, BiPredicate<TransactionRequest, UserAccountBalanceInfo>> validationMap;

    public TransactionCrudServiceImpl(TransactionRepository transactionRepository, ApprovedTransactionMapper approvedTransactionMapper) {
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

        final Optional<UserAccountBalanceInfo> userAccountBalanceInfo = transactionRepository.fetchLatestStatusForUserAndAccount(request.userId(),
                request.accountNumber());
        final UserAccountBalanceInfo info = userAccountBalanceInfo.orElseThrow(() -> {
            log.warn("no matching account found for userId '{}' and account number '{}'", request.userId(), request.accountNumber());
            return new UserNotFoundException();
        });

        performBusinessValidation(request, info);

        final ApprovedTransaction transaction = approvedTransactionMapper.build(request, info);
        transactionRepository.updateBalanceForTransaction(transaction);
        log.info("Successfully created transaction for user '{}' and account '{}' with  transaction id '{}", transaction.userIdentifier().externalUserId(),
                transaction.accountIdentifier().accountNumber(), transaction.transactionId().externalId());
        return transaction;
    }

    private void performBusinessValidation(TransactionRequest request, UserAccountBalanceInfo info) {
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
