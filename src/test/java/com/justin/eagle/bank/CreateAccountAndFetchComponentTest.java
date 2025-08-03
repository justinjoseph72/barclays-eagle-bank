package com.justin.eagle.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

public class CreateAccountAndFetchComponentTest extends BaseComponentTest {

    @Test
    void verifyInvalidTokenForCreateAccountWillReturn401Error() throws Exception {
        var payload = getNewAccountPayloadForAccountName("Savings Acc");
        mockMvc.perform(post("/v1/accounts")
                        .contentType("application/json")
                        .header("Authorization", "authToken")
                        .content(payload)
                )
                .andExpect(status().is(401));
    }

    @Test
    void verifyExpiredTokenForACreateAccountWillReturn401Error() throws Exception {
        var payload = getNewAccountPayloadForAccountName("Savings Acc");
        mockMvc.perform(post("/v1/accounts")
                        .contentType("application/json")
                        .header("Authorization", expiredToken)
                        .content(payload)
                )
                .andExpect(status().is(401));
    }

    @Test
    void verifyAccountIsCreatedSuccessfullyForAValidUser() throws Exception {
        final String userId = given_a_new_user_is_created();

        final String authToken = and_an_auth_token_is_generated_for_the_user(userId);
        var payload = getNewAccountPayloadForAccountName("Savings Acc");
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
    }

    @Test
    void verifyCreatedAccountIsSuccessfullyFetched() throws Exception {
        final String userId = given_a_new_user_is_created();

        final String accountNumber = and_an_account_is_created_with_name(userId, "Super saver");

        final String authToken = and_an_auth_token_is_generated_for_the_user(userId);

        final String response = mockMvc.perform(get("/v1/accounts/{accountNumber}", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                )
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andReturn()
                .getResponse()
                .getContentAsString();
        System.out.println(response);

    }
}
