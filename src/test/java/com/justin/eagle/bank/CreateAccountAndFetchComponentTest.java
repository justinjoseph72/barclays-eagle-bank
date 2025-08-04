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

        System.out.println(accountNumber);

        final String response = mockMvc.perform(get("/v1/accounts/{accountNumber}", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                )
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.name").value("Super saver"))
                .andExpect(jsonPath("$.accountType").value("personal"))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    void verifyAllTheAccountForAUserAreFetched() throws Exception {

        final String userId = given_a_new_user_is_created();
        final String authToken = and_an_auth_token_is_generated_for_the_user(userId);

        final String accountNumber1 = and_an_account_is_created_with_name(userId, "Super saver");
        final String accountNumber2 = and_an_account_is_created_with_name(userId, "Current");

        final String response = mockMvc.perform(get("/v1/accounts")
                        .contentType("application/json")
                        .header("Authorization", authToken)
                )
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.accounts[0].accountNumber").value(accountNumber1))
                .andExpect(jsonPath("$.accounts[1].accountNumber").value(accountNumber2))
                .andReturn()
                .getResponse()
                .getContentAsString();

    }

    @Test
    void verifyFetchingDifferentUserAccountWillReturnError() throws Exception {

        final String userId_1 = given_a_new_user_is_created();
        final String authToken1 = and_an_auth_token_is_generated_for_the_user(userId_1);

        final String accountNumber1 = and_an_account_is_created_with_name(userId_1, "Super saver");

        final String userId_2 = given_a_new_user_is_created();
        final String authToken2 = and_an_auth_token_is_generated_for_the_user(userId_2);
        final String accountNumber2 = and_an_account_is_created_with_name(userId_2, "Current");

        mockMvc.perform(get("/v1/accounts/{accountNumber}", accountNumber1)
                        .contentType("application/json")
                        .header("Authorization", authToken2)
                )
                .andExpect(status().is(401));


        mockMvc.perform(get("/v1/accounts/{accountNumber}", accountNumber2)
                        .contentType("application/json")
                        .header("Authorization", authToken1)
                )
                .andExpect(status().is(401));
    }
}
