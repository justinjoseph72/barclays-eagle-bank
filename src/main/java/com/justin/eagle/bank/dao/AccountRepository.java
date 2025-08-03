package com.justin.eagle.bank.dao;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.justin.eagle.bank.domain.ActiveAccount;
import com.justin.eagle.bank.utl.IdSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AccountRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final IdSupplier idSupplier;
    private final Predicate<String> isInvalidAccountNumber =
            Predicate.not(Pattern.compile("^01\\d{6}$").asMatchPredicate());

    private static final String INSERT_ACCOUNT_RECORD = """
            insert into account (id, account_number, sort_code, party_id, status, type, name,record_creation_timestamp)
            values (:id, :accountNumber, :sortCode, :partyId, :status, :type, :name,:recordCreationTimestamp)
            """;

    private static final String INSERT_BALANCE_SQL = """
            insert into balance (account_id, currency, amount, record_timestamp)
            values (:accountId, :currency, :amount, :recordTimestamp)
            """;

    private static final String FETCH_ALL_ACCOUNTS_FOR_USER_SQL = """
            with s1 as (select *, row_number() over (partition by id order by record_creation_timestamp desc) as row_num,
             min(record_creation_timestamp) over (partition by id) as account_creation_timestamp,
             max(record_creation_timestamp) over (partition by id) as account_last_updated_timestamp
              from account where party_id = :partyId),
             latest_account_details as (select * from s1 where row_num = 1)
            select lad.*,amount, currency,
            greatest(b.record_timestamp, lad.account_last_updated_timestamp) as last_updated_timestamp
             from latest_account_details lad join balance b on b.account_id = lad.id
            """;

    private static final String FETCH_ACCOUNT_DETAIL_SQL = """
            with s1 as (select *, row_number() over (partition by id order by record_creation_timestamp desc) as row_num,
             min(record_creation_timestamp) over (partition by id) as account_creation_timestamp,
             max(record_creation_timestamp) over (partition by id) as account_last_updated_timestamp
              from account where account_number = :accountNumber),
             latest_account_details as (select * from s1 where row_num = 1)
            select lad.*,amount, currency,
            greatest(b.record_timestamp, lad.account_last_updated_timestamp) as last_updated_timestamp
             from latest_account_details lad join balance b on b.account_id = lad.id
            """;
    private final RowMapper<ActiveAccount> activeAccountRowMapper;

    public AccountRepository(NamedParameterJdbcTemplate jdbcTemplate, IdSupplier idSupplier, RowMapper<ActiveAccount> activeAccountRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.idSupplier = idSupplier;
        this.activeAccountRowMapper = activeAccountRowMapper;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String getNewAccountNumber() {
        final Integer nextSequence = jdbcTemplate.queryForObject("select nextval('account_number_seq')", Map.of(), Integer.class);
        final String newAccountNumber = idSupplier.getNewAccountNumber(nextSequence);
        if (isInvalidAccountNumber.test(newAccountNumber)) {
            log.error("the generated account number '{}' is not valid format", newAccountNumber);
            throw new IllegalStateException("reached limit for creating account numbers");
        }
        return newAccountNumber;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void persistNewAccount(ActiveAccount activeAccount) {
        var param = new MapSqlParameterSource();
        param.addValue("id", activeAccount.identifier().id());
        param.addValue("accountNumber", activeAccount.identifier().accountNumber());
        param.addValue("sortCode", activeAccount.identifier().sortCode());
        param.addValue("partyId", activeAccount.partyId());
        param.addValue("status", "ACTIVE");
        param.addValue("type", activeAccount.type());
        param.addValue("name", activeAccount.name());
        param.addValue("recordCreationTimestamp", Timestamp.from(activeAccount.auditData().createdTimestamp()), Types.TIMESTAMP);

        jdbcTemplate.update(INSERT_ACCOUNT_RECORD, param);

        var balanceParam = new MapSqlParameterSource();
        balanceParam.addValue("accountId", activeAccount.identifier().id());
        balanceParam.addValue("currency", activeAccount.currentBalance().currency());
        balanceParam.addValue("amount", activeAccount.currentBalance().amount());
        balanceParam.addValue("recordTimestamp", Timestamp.from(activeAccount.auditData().createdTimestamp()), Types.TIMESTAMP);

        jdbcTemplate.update(INSERT_BALANCE_SQL, balanceParam);

    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ActiveAccount> findAllAccountsForUser(UUID partyId) {
        try {
            var param = new MapSqlParameterSource();
            param.addValue("partyId", partyId);
            return jdbcTemplate.query(FETCH_ALL_ACCOUNTS_FOR_USER_SQL, param, activeAccountRowMapper);
        } catch (Exception e) {
            log.warn("error fetching account details for party id  '{}'", partyId);
            throw new DatabaseInteractionException(e);
        }
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<ActiveAccount> findAccountDetail(String accountNumber) {
        try {
            var param = new MapSqlParameterSource();
            param.addValue("accountNumber", accountNumber);
            return Optional.ofNullable(jdbcTemplate.queryForObject(FETCH_ACCOUNT_DETAIL_SQL, param, activeAccountRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.warn("error fetching account details for account  '{}'", accountNumber);
            throw new DatabaseInteractionException(e);
        }

    }
}

