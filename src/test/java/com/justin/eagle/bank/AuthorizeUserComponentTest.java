package com.justin.eagle.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

public class AuthorizeUserComponentTest extends BaseComponentTest {

    @Test
    void verifyErrorIsReturnedWhenUnknownUserAttemptsToBuildAJwtToken() throws Exception {
        mockMvc.perform(post("/v1/users/{userId}/authorize", "usr-someUserId")
                        .contentType("application/json")
                )
                .andExpect(status().is(401));
    }
}
