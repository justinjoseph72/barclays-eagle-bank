package com.justin.eagle.bank.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.justin.eagle.bank.dao.model.AccountStatusInfo;
import com.justin.eagle.bank.dao.model.UserAccountBalanceInfo;
import com.justin.eagle.bank.dao.model.UserStatusDbInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
class UserAccountBalanceInfoRowMapper implements RowMapper<UserAccountBalanceInfo> {

    @Override
    public UserAccountBalanceInfo mapRow(ResultSet rs, int rowNum) throws SQLException {

        return UserAccountBalanceInfo.builder()
                .userInfo(UserStatusDbInfo.builder()
                        .partyId(UUID.fromString(rs.getString("party_id")))
                        .userId(rs.getString("external_id"))
                        .status(rs.getString("party_status"))
                        .build())
                .accountInfo(AccountStatusInfo.builder()
                        .accountId(UUID.fromString(rs.getString("account_id")))
                        .accountNumber(rs.getString("account_number"))
                        .status(rs.getString("status"))
                        .build())
                .currentBalance(new BigDecimal(rs.getString("amount")))
                .currency(rs.getString("currency"))
                .build();
    }
}
