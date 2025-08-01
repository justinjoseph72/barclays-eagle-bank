package com.justin.eagle.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;


public class CreateUserComponentTest extends BaseComponentTest {



    @Test
    void shouldVerifyEmptyStringInThePayloadForCreateUserWillReturn400Error() throws Exception {

        String payload = """
                """;
        mockMvc.perform(post("/v1/users")
                        .contentType("application/json")
                        .content(payload)
                )
                .andExpect(status().is(400));


    }

    @Test
    void shouldVerifyValidPayloadForCreateUserWillReturn501Error() throws Exception {

        String payload = """
                {
                  "name": "Test User",
                  "address": {
                    "line1": "string",
                    "line2": "string",
                    "line3": "string",
                    "town": "string",
                    "county": "string",
                    "postcode": "string"
                  },
                  "phoneNumber": "string",
                  "email": "user@example.com"
                }
                """;
        mockMvc.perform(post("/v1/users")
                        .contentType("application/json")
                        .content(payload)
                )
                .andExpect(status().is(501));


    }

}
