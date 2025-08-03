package com.justin.eagle.bank.dao;

import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

import com.justin.eagle.bank.dao.model.UserStatusDbInfo;
import com.justin.eagle.bank.domain.ProvisionedUser;
import com.justin.eagle.bank.domain.UserIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
    private static final String GET_USER_STATUS_QUERY = """
            select id, external_id, status from party where external_id = :userId order by record_creation_timestamp desc limit 1
            """;

    private static final String FETCH_LATEST_PARTY_DETAILS_QUERY = """
                with latest_party as (select id, external_id,status, record_creation_timestamp as party_update_timestamp,
                 min(record_creation_timestamp) over (partition by id) as party_creation_timestamp from party
                where external_id = :userId order by record_creation_timestamp desc limit 1),
                latest_profile as (select name, phone_number, email, record_creation_timestamp as profile_update_timestamp
                from party_profile pp join latest_party lp on lp.id = pp.party_id order by record_creation_timestamp desc limit 1),
                latest_address as (select line1, line2,line3, county,town,postcode, record_creation_timestamp as address_update_timestamp
                from party_address pa join latest_party lp on lp.id = pa.party_id order by record_creation_timestamp desc limit 1)
                select latest_party.*, latest_profile.*,latest_address.*,
                 greatest(party_update_timestamp,profile_update_timestamp,address_update_timestamp) as last_updated_timestamp
                 from latest_party,latest_profile,latest_address
                """;
    private final RowMapper<ProvisionedUser> userFetchRowMapper;

    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<ProvisionedUser> userFetchRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userFetchRowMapper = userFetchRowMapper;
    }

    @Transactional
    public void saveUser(ProvisionedUser user) {

        var param = new MapSqlParameterSource();
        final UserIdentifier identifier = user.identifier();
        param.addValue("id", identifier.partyId());
        param.addValue("externalId", identifier.externalUserId());
        param.addValue("status", "ACTIVE");
        param.addValue("recordCreationTimestamp", Timestamp.from(user.auditData().createdTimestamp()), Types.TIMESTAMP);
        jdbcTemplate.update(INSERT_USER_SQL, param);

        var profileParam = new MapSqlParameterSource();
        profileParam.addValue("partyId", identifier.partyId());
        profileParam.addValue("name", user.info().profile().name());
        profileParam.addValue("phoneNumber", user.info().profile().phoneNumber());
        profileParam.addValue("email", user.info().profile().emailAddress());
        profileParam.addValue("recordCreationTimestamp", Timestamp.from(user.auditData().createdTimestamp()), Types.TIMESTAMP);

        jdbcTemplate.update(INSERT_PROFILE_SQL, profileParam);

        var addressParam = new MapSqlParameterSource();
        addressParam.addValue("partyId", identifier.partyId());
        addressParam.addValue("line1", user.info().address().addressLines().get(0));
        addressParam.addValue("line2", user.info().address().addressLines().get(1));
        addressParam.addValue("line3", user.info().address().addressLines().get(2));

        addressParam.addValue("town", user.info().address().town());
        addressParam.addValue("county", user.info().address().county());
        addressParam.addValue("postcode", user.info().address().postCode());
        addressParam.addValue("recordCreationTimestamp", Timestamp.from(user.auditData().createdTimestamp()), Types.TIMESTAMP);
        jdbcTemplate.update(INSERT_ADDRESS_SQL, addressParam);
    }

    @Transactional(readOnly = true, isolation = REPEATABLE_READ)
    public Optional<UserStatusDbInfo> getUserStatusInfo(String userId) {
        var param = new MapSqlParameterSource();
        param.addValue("userId", userId);
        try {
            return jdbcTemplate.queryForObject(GET_USER_STATUS_QUERY, param, (rs, rowNum) -> {
                return Optional.of(UserStatusDbInfo.builder()
                        .partyId(UUID.fromString(rs.getString("id")))
                        .userId(rs.getString("external_id"))
                        .status(rs.getString("status"))
                        .build());
            });
        } catch (EmptyResultDataAccessException e) {
            log.info("no records found for user id '{}'", userId);
            return Optional.empty();
        } catch (Exception e) {
            throw new DatabaseInteractionException(e);
        }
    }

    @Transactional(readOnly = true, isolation = REPEATABLE_READ)
    public Optional<ProvisionedUser> fetchLatestUserDetails(String userId) {
        var param = new MapSqlParameterSource();
        param.addValue("userId", userId);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FETCH_LATEST_PARTY_DETAILS_QUERY, param, userFetchRowMapper));

        }catch (EmptyResultDataAccessException e) {
            log.info("no records found for user id '{}'", userId);
            return Optional.empty();
        }
    }
}
