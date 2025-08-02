package com.justin.eagle.bank.dao;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.justin.eagle.bank.domain.ActiveAccount;
import com.justin.eagle.bank.utl.IdSupplier;
import lombok.extern.slf4j.Slf4j;
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

    private static final String INSERT_BALACE_SQL = """
            insert into balance (account_id, currency, amount, record_timestamp)
            values (:accountId, :currency, :amount, :recordTimestamp)
            """;

    public AccountRepository(NamedParameterJdbcTemplate jdbcTemplate, IdSupplier idSupplier) {
        this.jdbcTemplate = jdbcTemplate;
        this.idSupplier = idSupplier;
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
        param.addValue("id", activeAccount.id());
        param.addValue("accountNumber", activeAccount.identifier().accountNumber());
        param.addValue("sortCode", activeAccount.identifier().sortCode());
        param.addValue("partyId", activeAccount.partyId());
        param.addValue("status", "ACTIVE");
        param.addValue("type", activeAccount.type());
        param.addValue("name", activeAccount.name());
        param.addValue("recordCreationTimestamp", Timestamp.from(activeAccount.auditData().createdTimestamp()), Types.TIMESTAMP);

        jdbcTemplate.update(INSERT_ACCOUNT_RECORD, param);

        var balanceParam = new MapSqlParameterSource();
        balanceParam.addValue("accountId", activeAccount.id());
        balanceParam.addValue("currency", activeAccount.currentBalance().currency());
        balanceParam.addValue("amount", activeAccount.currentBalance().amount());
        balanceParam.addValue("recordTimestamp", Timestamp.from(activeAccount.auditData().createdTimestamp()), Types.TIMESTAMP);

        jdbcTemplate.update(INSERT_BALACE_SQL, balanceParam);

    }
}

