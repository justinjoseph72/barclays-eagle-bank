package com.justin.eagle.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

public class CreateAndFetchTransactionComponentTest extends BaseComponentTest {

    @Test
    void verifyDepositAndWithdrawTransactionCanBeMadeToAnAccountAndIsReflectedToTheAccount() throws Exception {
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

        var creditResponse1 = mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                .contentType("application/json")
                .header("Authorization", authToken)
                .content(transactionPayload))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(100.67))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andReturn().getResponse().getContentAsString();
        final String txId1 = JsonPath.parse(creditResponse1).read("$.id").toString();

        //Checking balance is updated in the account

        verifyAccountBalance(accountNumber, authToken, 100.67);

        //another credit transaction

        var depositTransaction_2 = """
                {
                  "amount": 50.97,
                  "currency": "GBP",
                  "type": "deposit",
                  "reference": "2nd deposit"
                }
                """;

        var creditResponse2 = mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                        .content(depositTransaction_2))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(50.97))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andReturn().getResponse().getContentAsString();
        final String txId2 = JsonPath.parse(creditResponse2).read("$.id").toString();

        // checking balance is update
        verifyAccountBalance(accountNumber, authToken, 151.64);

        var withDrawTransaction = """
                {
                  "amount": 30.24,
                  "currency": "GBP",
                  "type": "withdrawal",
                  "reference": "withdrawal"
                }
                """;

        var withDrawResponse = mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                        .content(withDrawTransaction))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(30.24))
                .andExpect(jsonPath("$.type").value("withdrawal"))
                .andReturn().getResponse().getContentAsString();
        final String txId3 = JsonPath.parse(withDrawResponse).read("$.id").toString();

        verifyAccountBalance(accountNumber, authToken, 121.40);

        //fetching all the transactions for the account
        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.transactions[0].id").value(txId3))
                .andExpect(jsonPath("$.transactions[1].id").value(txId2))
                .andExpect(jsonPath("$.transactions[2].id").value(txId1));

    }

    @Test
    void verifyAccountShouldNotBeAllowedToGoToNegativeBalance() throws Exception {
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

        var response1 = mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                        .content(transactionPayload))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(100.67))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andReturn().getResponse().getContentAsString();
        final String txId1 = JsonPath.parse(response1).read("$.id").toString();
        System.out.println(txId1);

        //fetching transaction details
        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}", accountNumber, txId1)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                        .content(transactionPayload))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(txId1))
                .andExpect(jsonPath("$.amount").value(100.67))
                .andExpect(jsonPath("$.type").value("deposit"));

        //Checking balance is updated in the account

        verifyAccountBalance(accountNumber, authToken, 100.67);

        //first withdrawal

        var withDrawTransaction = """
                {
                  "amount": 30.24,
                  "currency": "GBP",
                  "type": "withdrawal",
                  "reference": "withdrawal"
                }
                """;

        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                        .content(withDrawTransaction))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(30.24))
                .andExpect(jsonPath("$.type").value("withdrawal"));

        verifyAccountBalance(accountNumber, authToken, 70.43);

        var withDrawTransaction_2 = """
                {
                  "amount": 75.24,
                  "currency": "GBP",
                  "type": "withdrawal",
                  "reference": "withdrawal"
                }
                """;

        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                        .content(withDrawTransaction_2))
                .andExpect(status().is(422));

        // balance is same as before
        verifyAccountBalance(accountNumber, authToken, 70.43);

        var withDrawTransaction_3 = """
                {
                  "amount": 70.43,
                  "currency": "GBP",
                  "type": "withdrawal",
                  "reference": "withdrawal"
                }
                """;

        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                        .content(withDrawTransaction_3))
                .andExpect(status().is(201));

        // balance can go to zero
        verifyAccountBalance(accountNumber, authToken, 0.0);
    }

    private void verifyAccountBalance(String accountNumber, String authToken, double expectedBalance) throws Exception {
        mockMvc.perform(get("/v1/accounts/{accountNumber}", accountNumber)
                        .contentType("application/json")
                        .header("Authorization", authToken)
                )
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.balance").value(expectedBalance));
    }
}
