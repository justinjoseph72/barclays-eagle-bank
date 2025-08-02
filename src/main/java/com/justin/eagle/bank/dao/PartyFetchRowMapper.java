package com.justin.eagle.bank.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import com.justin.eagle.bank.user.model.NewUser;
import com.justin.eagle.bank.user.model.ProvisionedUser;
import com.justin.eagle.bank.user.model.UserAddress;
import com.justin.eagle.bank.user.model.UserProfile;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
class PartyFetchRowMapper implements RowMapper<ProvisionedUser> {

    @Override
    public ProvisionedUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        final Instant lastUpdatedTimestamp = Stream.of(rs.getTimestamp("party_update_timestamp"), rs.getTimestamp("profile_update_timestamp"),
                        rs.getTimestamp("address_update_timestamp"))
                .filter(Objects::nonNull)
                .map(Timestamp::toInstant)
                .max(Instant::compareTo).orElse(null);
        return ProvisionedUser.builder()
                .userId(UUID.fromString(rs.getString("id")))
                .externalUserId(rs.getString("external_id"))
                .createdTimestamp(rs.getTimestamp("party_creation_timestamp").toInstant())
                .updatedTimestamp(lastUpdatedTimestamp)
                .user(NewUser.builder()
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
