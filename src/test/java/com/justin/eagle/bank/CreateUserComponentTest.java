package com.justin.eagle.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    void shouldVerifyValidPayloadForCreateUserWillReturn201Error() throws Exception {

        String payload = provideCreateUserTemplate()
                .replace("replaceName", "someName")
                .replace("replaceTown", "someTown")
                .replace("replaceCounty", "Essex")
                .replace("replacePostcode", "SS3333")
                .replace("replacePhoneNumber", "+44-034333434")
                .replace("replaceEmail", "user@something.com");
        mockMvc.perform(post("/v1/users")
                        .contentType("application/json")
                        .content(payload)
                )
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("someName"))
                .andExpect(jsonPath("$.email").value("user@something.com"));


    }


    private String provideCreateUserTemplate() {
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



}
