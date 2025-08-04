package com.justin.eagle.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class CreateAndFetchTransactionComponentTest extends BaseComponentTest {

    @Test
    void verifyDepositTransactionCanBeMadeToAnAccountAndIsReflectedToTheAccount() throws Exception {
        final String userId = given_a_new_user_is_created();
        final String authToken = and_an_auth_token_is_generated_for_the_user(userId);

        final String accountNumber = and_an_account_is_created_with_name(userId, "Super saver");

        var transactionPayload = """
                {
                  "amount": 100.67,
                  "currency": "GBP",
                  "type": "deposit",
                  "reference": "first deposit"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                .contentType("application/json")
                .header("Authorization", authToken)
                .content(transactionPayload))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(100.67))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andExpect(jsonPath("$.reference").value("first deposit"));

        //Checking balance is updated in the account

        mockMvc.perform(get("/v1/accounts/{accountNumber}", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                )
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.balance").value(100.67));

        //another credit transaction

        var transactionPayload_2 = """
                {
                  "amount": 50.97,
                  "currency": "GBP",
                  "type": "deposit",
                  "reference": "2nd deposit"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                        .content(transactionPayload_2))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(50.97))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andExpect(jsonPath("$.reference").value("2nd deposit"));

        // checking balance is update
        mockMvc.perform(get("/v1/accounts/{accountNumber}", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                )
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.balance").value(151.64));



    }
}
