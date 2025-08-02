package com.justin.eagle.bank.dao;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.stream.IntStream;

import com.justin.eagle.bank.user.model.ProvisionedUser;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT_USER_SQL = """
            insert into party (id, external_id,status,record_creation_timestamp) values
            (:id, :externalId, :status, :recordCreationTimestamp)
            """;

    private static final String INSERT_PROFILE_SQL = """
            insert into party_profile (party_id, name, phone_number, email, record_creation_timestamp) values
            (:partyId, :name, :phoneNumber, :email, :recordCreationTimestamp)
            """;

    private static final String INSERT_ADDRESS_SQL = """
         insert into party_address (party_id, line1, line2,line3,town,county,postcode,record_creation_timestamp) values
         (:partyId, :line1, :line2,:line3,:town,:county,:postcode,:recordCreationTimestamp)
         """;

    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void saveUser(ProvisionedUser user) {

        var param = new MapSqlParameterSource();
        param.addValue("id", user.userId());
        param.addValue("externalId", user.externalUserId());
        param.addValue("status", "ACTIVE");
        param.addValue("recordCreationTimestamp", Timestamp.from(user.createdTimestamp()), Types.TIMESTAMP);
        jdbcTemplate.update(INSERT_USER_SQL, param);

        var profileParam = new MapSqlParameterSource();
        profileParam.addValue("partyId", user.userId());
        profileParam.addValue("name", user.user().profile().name());
        profileParam.addValue("phoneNumber", user.user().profile().phoneNumber());
        profileParam.addValue("email", user.user().profile().emailAddress());
        profileParam.addValue("recordCreationTimestamp", Timestamp.from(user.createdTimestamp()), Types.TIMESTAMP);

        jdbcTemplate.update(INSERT_PROFILE_SQL, profileParam);

        var addressParam = new MapSqlParameterSource();
        addressParam.addValue("partyId", user.userId());
        addressParam.addValue("line1"  , user.user().address().addressLines().get(0));
        addressParam.addValue("line2"  , user.user().address().addressLines().get(1));
        addressParam.addValue("line3"  , user.user().address().addressLines().get(2));

        addressParam.addValue("town", user.user().address().town());
        addressParam.addValue("county", user.user().address().county());
        addressParam.addValue("postcode", user.user().address().postCode());
        addressParam.addValue("recordCreationTimestamp", Timestamp.from(user.createdTimestamp()), Types.TIMESTAMP);
        jdbcTemplate.update(INSERT_ADDRESS_SQL, addressParam);


    }
}
