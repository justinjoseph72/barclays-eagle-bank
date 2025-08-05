package com.justin.eagle.bank;

import static com.justin.eagle.bank.matcher.TimestampComparator.forPaths;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

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

    @Test
    void shouldVerifyFetchUserReturnsValidData() throws Exception {

        String payload = provideCreateUserTemplate()
                .replace("replaceName", "someNewName")
                .replace("replaceTown", "someNewTown")
                .replace("replaceCounty", "Essex")
                .replace("replacePostcode", "SS3333")
                .replace("replacePhoneNumber", "+44-134333434")
                .replace("replaceEmail", "user1@something1.com");
        final String createUserResponse = mockMvc.perform(post("/v1/users")
                        .contentType("application/json")
                        .content(payload)
                )
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn()
                .getResponse().getContentAsString();
        final String userId = JsonPath.parse(createUserResponse).read("$.id").toString();
        System.out.println("the user id is " + userId);
        System.out.println(createUserResponse);

        final String bearerTokenForUser = and_an_auth_token_is_generated_for_the_user(userId);

        final String fetchResult = mockMvc.perform(get("/v1/users/{userId}", userId)
                        .header("Authorization", bearerTokenForUser))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse().getContentAsString();
        System.out.println(fetchResult);

        JSONAssert.assertEquals(createUserResponse, fetchResult, forPaths("createdTimestamp", "updatedTimestamp"));

    }








}
