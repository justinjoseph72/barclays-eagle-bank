package com.justin.eagle.bank.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import com.justin.eagle.bank.domain.AuditData;
import com.justin.eagle.bank.domain.UserIdentifier;
import com.justin.eagle.bank.domain.UserInfo;
import com.justin.eagle.bank.domain.ProvisionedUser;
import com.justin.eagle.bank.domain.UserAddress;
import com.justin.eagle.bank.domain.UserProfile;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
class PartyFetchRowMapper implements RowMapper<ProvisionedUser> {

    @Override
    public ProvisionedUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ProvisionedUser.builder()
                .identifier(UserIdentifier.builder()
                        .partyId(UUID.fromString(rs.getString("id")))
                        .externalUserId(rs.getString("external_id"))
                        .build())
                .auditData(AuditData.builder()
                        .createdTimestamp(rs.getTimestamp("party_creation_timestamp").toInstant())
                        .lastUpdatedTimestamp(rs.getTimestamp("last_updated_timestamp").toInstant())
                        .build())
                .info(UserInfo.builder()
                        .profile(UserProfile.builder()
                                .name(rs.getString("name"))
                                .phoneNumber(rs.getString("phone_number"))
                                .emailAddress(rs.getString("email"))
                                .build())
                        .address(UserAddress.builder()
                                .town(rs.getString("town"))
                                .county(rs.getString("county"))
                                .postCode(rs.getString("postcode"))
                                .addressLines(Stream.of(rs.getString("line1"),
                                        rs.getString("line2"), rs.getString("line3"))
                                        .filter(Objects::nonNull)
                                        .toList())
                                .build())
                        .build())

                .build();
    }
}
