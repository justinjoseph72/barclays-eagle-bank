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

    protected String expiredToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c3Itc29tZVVzZXJJZCIsImlhdCI6MTc1NDA3ODA4NSwiZXhwIjoxNzU0MDc4Mzg1fQ.for0K8Jl9R5vgQmxsTMYJ4s0upxDOXf2PSUe7F4oYrkPMZ_Acrn3VMxHvMUvHeewyR3GzY9w0qt9mySc-BkpcQ";

    protected String provideCreateUserTemplate() {
        return """
                {
                  "name": "replaceName",
                  "address": {
                    "line1": "replaceLine1",
                    "line2": "replaceLine2",
                    "line3": "replaceLine2",
                    "town": "replaceTown",
                    "county": "replaceCounty",
                    "postcode": "replacePostcode"
                  },
                  "phoneNumber": "replacePhoneNumber",
                  "email": "replaceEmail"
                }
                """;
    }

    protected String given_a_new_user_is_created() throws Exception {
        String payload = provideCreateUserTemplate()
                .replace("replaceName", "someNewName")
                .replace("replaceTown", "someNewTown")
                .replace("replaceCounty", "Essex")
                .replace("replacePostcode", "SS3333")
                .replace("replacePhoneNumber", "+44-134333434")
                .replace("replaceEmail", "user1@something1.com");
        final String createUserResponse = mockMvc.perform(post("/v1/users")
                        .contentType("application/json")
                        .content(payload)
                )
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn()
                .getResponse().getContentAsString();
        final String userId = JsonPath.parse(createUserResponse).read("$.id").toString();
        System.out.println("the user id is " + userId);
        return userId;
    }

    protected String and_an_auth_token_is_generated_for_the_user(String userId) throws Exception {
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

    protected String getNewAccountPayloadForAccountName(String name) {
        String accountPayloadTemplate = """
                { "name": "replaceName",
                   "accountType": "personal"
                }
                """;
        return accountPayloadTemplate.replace("replaceName", name);
    }

}
