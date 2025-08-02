package com.justin.eagle.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

public class CreateAccountComponentTest extends BaseComponentTest {

    @Test
    void verifyAccountIsCreatedSuccessfullyForAValidUser() throws Exception {
        final String userId = given_a_new_user_is_created();

        final String authToken = and_an_auth_token_is_generated_for_the_user(userId);

        String accountPayloadTemplate = """
               { "name": "replaceName",
                  "accountType": "personal"
               }
               """;
        var payload = accountPayloadTemplate.replace("replaceName", "Savings Acc");
        final String createUserResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType("application/json")
                        .header("Authorization", authToken)
                        .content(payload)
                )
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.accountNumber").isNotEmpty())
                .andExpect(jsonPath("$.balance").value(0.0))
                .andReturn()
                .getResponse().getContentAsString();
        System.out.println(createUserResponse);


    }
}
