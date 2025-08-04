package com.justin.eagle.bank.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.justin.eagle.bank.dao.model.TransactionLog;
import com.justin.eagle.bank.dao.model.UserAccountBalanceInfo;
import com.justin.eagle.bank.domain.ApprovedTransaction;
import com.justin.eagle.bank.domain.CreditTransaction;
import com.justin.eagle.bank.domain.DebitTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
public class TransactionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserAccountBalanceInfoRowMapper usrAccountBalanceInfoRowMapper;
    private final TransactionLogRowMapper transactionRowMapper;

    private static final String LOCK_BALANCE_FOR_ACCOUNT = """
            select * from balance where account_id = :accountId for update nowait
            """;

    private static final String INSERT_TRANSACTION_LOG =
            """
                    insert into transaction_log (
                    id,transaction_id,is_credit,reference,party_id,account_id,status,currency,amount,running_balance,record_creation_timestamp)
                    values (:id,:transactionId,:isCredit,:reference,:partyId,:accountId,:status,:currency,:amount,:runningBalance,:recordCreationTimestamp)""";

    private static final String UPDATE_BALANCE_CREDIT = """
            update balance set amount = amount + :transactionAmount,
             where account_id = :accountId
             returning amount as updatedAmount""";

    private static final String UPDATE_BALANCE_DEBIT = """
            update balance set amount = amount - :transactionAmount where account_id = :accountId
            and amount >= :transactionAmount returning amount as updatedAmount""";
    private static final String FETCH_USER_ACCOUNT_DETAIL_SQL = """
            with latest_user_info as (select id as party_id, external_id, status as party_status
             from party where external_id = :userId order by record_creation_timestamp desc limit 1),
            latest_user_account_info as (select lui.*, a.id as account_id, a,account_number, a.status as account_status from account a join
             latest_user_info lui on lui.party_id = a.party_id where account_number = :accountNumber order by a.record_creation_timestamp desc limit 1)
             select luai.*, b.amount,b.currency from latest_user_account_info luai join balance b on luai.account_id = b.account_id
            """;

    private static final String FETCH_ALL_TRANSACTIONS_SQL = "select * from transaction_log where party_id : partyId and account_id = :accountId order by record_creation_timestamp desc";
    private static final String FETCH_TRANSACTION_SQL = "select * from transaction_log where party_id : partyId and account_id = :accountId and transaction_id = :transactionId";


    public TransactionRepository(NamedParameterJdbcTemplate jdbcTemplate, UserAccountBalanceInfoRowMapper usrAccountBalanceInfoRowMapper,
            TransactionLogRowMapper transactionRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.usrAccountBalanceInfoRowMapper = usrAccountBalanceInfoRowMapper;
        this.transactionRowMapper = transactionRowMapper;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateBalanceForTransaction(ApprovedTransaction transaction) {

        final UUID accountId = transaction.accountIdentifier().id();
        var lockBalanceParam = new MapSqlParameterSource();
        lockBalanceParam.addValue("accountId", accountId);
        jdbcTemplate.update(LOCK_BALANCE_FOR_ACCOUNT, lockBalanceParam);

        var updateBalanceParam = new MapSqlParameterSource();
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        record TransactionSupport(String sql, Boolean creditDebitIndicator) {}

        TransactionSupport action = switch (transaction) {
            case CreditTransaction creditTransaction -> new TransactionSupport(UPDATE_BALANCE_CREDIT, true);
            case DebitTransaction debitTransaction -> new TransactionSupport(UPDATE_BALANCE_DEBIT, false);
        };

        final int updatedRows = jdbcTemplate.update(action.sql, updateBalanceParam, keyHolder);

        if (updatedRows != 1) {
            String message = "error %s account '%s' with amount '%s' for reference %s".formatted(action.creditDebitIndicator ? "crediting" : "debiting",
                    accountId, transaction.transactionAmount(), transaction.transactionId().reference());
            log.warn(message);
            throw new BalanceUpdateException(message);
        }

        final BigDecimal updatedAmount = new BigDecimal(Objects.requireNonNull(keyHolder.getKeyAs(String.class)));

        var transactionLogParam = new MapSqlParameterSource();
        transactionLogParam.addValue("id", transaction.transactionId().id());
        transactionLogParam.addValue("transactionId", transaction.transactionId().externalId());
        transactionLogParam.addValue("isCredit", action.creditDebitIndicator);
        transactionLogParam.addValue("reference", transaction.transactionId().reference());
        transactionLogParam.addValue("partyId", transaction.userIdentifier().partyId());
        transactionLogParam.addValue("accountId", transaction.accountIdentifier().id());
        transactionLogParam.addValue("status", "POSTED");
        transactionLogParam.addValue("currency", transaction.transactionAmount().currency());
        transactionLogParam.addValue("amount", transaction.transactionAmount().amount());
        transactionLogParam.addValue("runningBalance", updatedAmount);
        transactionLogParam.addValue("recordCreationTimestamp", transaction.auditData().createdTimestamp());

        jdbcTemplate.update(INSERT_TRANSACTION_LOG, transactionLogParam);

    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<UserAccountBalanceInfo> fetchLatestStatusForUserAndAccount(String userId, String accountNumber) {
        var param = new MapSqlParameterSource();
        param.addValue("userId", userId);
        param.addValue("accountNumber", accountNumber);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FETCH_USER_ACCOUNT_DETAIL_SQL, param, usrAccountBalanceInfoRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new DatabaseInteractionException(e);
        }
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<TransactionLog> fetchAllTransactions(UUID partyId, UUID accountId) {
        var param = new MapSqlParameterSource();
        param.addValue("partyId", partyId);
        param.addValue("accountId", accountId);
        try {
            return jdbcTemplate.query(FETCH_ALL_TRANSACTIONS_SQL, param, transactionRowMapper);
        } catch (Exception e) {
            throw new DatabaseInteractionException(e);
        }
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<TransactionLog> fetchTransaction(UUID partyId, UUID accountId, String transactionId) {
        var param = new MapSqlParameterSource();
        param.addValue("partyId", partyId);
        param.addValue("accountId", accountId);
        param.addValue("transactionId", transactionId);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FETCH_TRANSACTION_SQL, param, transactionRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new DatabaseInteractionException(e);
        }
    }
}
