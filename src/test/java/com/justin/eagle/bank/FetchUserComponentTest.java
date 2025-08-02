package com.justin.eagle.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

public class FetchUserComponentTest extends BaseComponentTest {

    // Scenario: User is only allowed to fetch its own user data. Accessing another user's data
    // is forbidden
    @Test
    void verifyAUserCannotUseItsTokenToFetchDetailsOfAnotherUser() throws Exception {
        final String firstUserId = given_a_new_user_is_created();
        final String secondUserId = given_a_new_user_is_created();

        final String firstUserToken = and_an_auth_token_is_generated_for_the_user(firstUserId);
        final String secondUserToken = and_an_auth_token_is_generated_for_the_user(secondUserId);

        // the second user's token is used to fetch the first user details
        mockMvc.perform(get("/v1/users/{userId}", firstUserId)
                        .header("Authorization", secondUserToken))
                .andExpect(status().is(401));

        mockMvc.perform(get("/v1/users/{userId}", firstUserId)
                        .header("Authorization", firstUserToken))
                .andExpect(status().is(200));

    }

}
