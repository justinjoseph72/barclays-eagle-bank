package com.justin.eagle.bank.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.justin.eagle.bank.dao.model.TransactionLog;
import com.justin.eagle.bank.domain.Amount;
import com.justin.eagle.bank.domain.AuditData;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
class TransactionLogRowMapper implements RowMapper<TransactionLog> {

    @Override
    public TransactionLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        return TransactionLog.builder()
                .id(UUID.fromString(rs.getString("id")))
                .transactionId(rs.getString("transaction_id"))
                .reference(rs.getString("reference"))
                .isCredit(rs.getString("is_credit").equals("t"))
                .amount(Amount.builder()
                        .amount(new BigDecimal(rs.getString("amount")))
                        .currency(rs.getString("currency"))
                        .build())
                .auditData(AuditData.builder()
                        .createdTimestamp(rs.getTimestamp("record_creation_timestamp").toInstant())
                        .build())
                .build();

    }

}
