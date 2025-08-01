package com.justin.eagle.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthorizeUserComponentTest extends BaseComponentTest {

    @Test
    void verifyTheAuthorizeUserHasReturnsAValidJwt() throws Exception {
        final String response = mockMvc.perform(post("/v1/users/{userId}/authorize", "usr-someUserId")
                        .contentType("application/json")
                )
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.userId").value("usr-someUserId"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.createdTimestamp").isNotEmpty())
                .andReturn()
                .getResponse().getContentAsString();

        final String jwtToken = JsonPath.parse(response).read("$.token").toString();
        final DecodedJWT decodedJwt = JWT.decode(jwtToken);
        final Instant instant = decodedJwt.getExpiresAt().toInstant();
        final Instant now = Instant.now(Clock.systemUTC());
        Assertions.assertThat(instant.isAfter(now)).isTrue();
        final Map<String, Claim> claims = decodedJwt.getClaims();
        Assertions.assertThat(claims.get("sub").asString()).isEqualTo("usr-someUserId");
    }
}
