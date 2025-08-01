package com.justin.eagle.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class CreateUserComponentTest {

    @Autowired MockMvc mockMvc;

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
