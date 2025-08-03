package com.justin.eagle.bank.dao;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.justin.eagle.bank.domain.AccountIdentifier;
import com.justin.eagle.bank.domain.ActiveAccount;
import com.justin.eagle.bank.domain.AuditData;
import com.justin.eagle.bank.domain.Amount;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
class AccountRowMapper implements RowMapper<ActiveAccount> {

    @Override
    public ActiveAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ActiveAccount.builder()
                .partyId(UUID.fromString(rs.getString("party_id")))
                .name(rs.getString("name"))
                .type(rs.getString("type"))
                .identifier(AccountIdentifier.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .sortCode(rs.getString("sort_code"))
                        .accountNumber(rs.getString("account_number"))
                        .build())
                .currentBalance(Amount.builder()
                        .currency(rs.getString("currency"))
                        .amount(new BigDecimal(rs.getString("amount")))
                        .build())
                .auditData(AuditData.builder()
                        .createdTimestamp(rs.getTimestamp("account_creation_timestamp").toInstant())
                        .lastUpdatedTimestamp(rs.getTimestamp("last_updated_timestamp").toInstant())
                        .build())
                .build();
    }
}
