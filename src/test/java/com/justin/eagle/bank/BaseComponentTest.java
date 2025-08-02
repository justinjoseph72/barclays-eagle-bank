package com.justin.eagle.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.justin.eagle.bank.embedded.EmbeddedServerRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class BaseComponentTest {

    static {
        EmbeddedServerRunner.initialize();
    }

    @Autowired
    protected MockMvc mockMvc;


    protected String getBearerTokenForUser(String userId) throws Exception {
        final String response = mockMvc.perform(post("/v1/users/{userId}/authorize", userId)
                        .contentType("application/json")
                )
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.createdTimestamp").isNotEmpty())
                .andReturn()
                .getResponse().getContentAsString();

        final String jwtToken = JsonPath.parse(response).read("$.token").toString();
        final String tokenType = JsonPath.parse(response).read("$.type").toString();
        return "%s %s".formatted(tokenType, jwtToken);
    }
}
