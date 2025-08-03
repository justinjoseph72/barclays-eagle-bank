package com.justin.eagle.bank.dao;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.justin.eagle.bank.domain.AccountIdentifier;
import com.justin.eagle.bank.domain.ActiveAccount;
import com.justin.eagle.bank.domain.AuditData;
import com.justin.eagle.bank.domain.Balance;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
class AccountRowMapper implements RowMapper<ActiveAccount> {


    @Override
    public ActiveAccount mapRow(ResultSet rs, int rowNum) throws SQLException {

        final Instant latestUpdatedTimestamp = Stream.of(rs.getTimestamp("account_last_updated_timestamp"), rs.getTimestamp("balance_updated_timestamp"))
                .filter(Objects::nonNull)
                .map(Timestamp::toInstant)
                .max(Instant::compareTo)
                .orElseThrow();

        return ActiveAccount.builder()
                .id(UUID.fromString(rs.getString("id")))
                .partyId(UUID.fromString(rs.getString("party_id")))
                .name(rs.getString("name"))
                .type(rs.getString("type"))
                .identifier(AccountIdentifier.builder()
                        .sortCode(rs.getString("sort_code"))
                        .accountNumber(rs.getString("account_number"))
                        .build())
                .currentBalance(Balance.builder()
                        .currency(rs.getString("currency"))
                        .amount(new BigDecimal(rs.getString("amount")))
                        .build())
                .auditData(AuditData.builder()
                        .createdTimestamp(rs.getTimestamp("account_creation_timestamp").toInstant())
                        .lastUpdatedTimestamp(latestUpdatedTimestamp)
                        .build())
                .build();
    }
}
