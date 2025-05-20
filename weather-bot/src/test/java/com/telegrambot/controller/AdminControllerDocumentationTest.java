package com.telegrambot.controller;

import com.telegrambot.config.TestMockConfig;
import com.telegrambot.config.WebConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Base64;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@ContextConfiguration(classes = {WebConfig.class, TestMockConfig.class})
@TestPropertySource(properties = {
    "admin.username=testadmin",
    "admin.password=testpass"
})
class AdminControllerDocumentationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;


    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @Test
    @DisplayName("Get Users - Successful Authentication")
    void getUsersSuccessfully() throws Exception {
        String credentials = adminUsername + ":" + adminPassword;
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        mockMvc.perform(get("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + base64Credentials))
                .andExpect(status().isOk())
                .andDo(document("get-users-success",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Credentials for Basic Authentication (format: 'Basic base64(username:password))')")
                        ),
                        responseFields(
                                fieldWithPath("[].telegramId")
                                        .description("User's Telegram ID"),
                                fieldWithPath("[].cityName")
                                        .description("User's city name"),
                                fieldWithPath("[].latitude")
                                        .description("Latitude of the user's location"),
                                fieldWithPath("[].longitude")
                                        .description("Longitude of the user's location"),
                                fieldWithPath("[].scheduleTime")
                                        .description("Notification time for weather updates"),
                                fieldWithPath("[].notifyCataclysm")
                                        .description("Whether the user is notified about cataclysms")
                        )));
    }

    @Test
    @DisplayName("Get Users - No Credentials")
    void getUsersUnauthorizedDueToNoCredentials() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized())
                .andDo(document("get-users-no-credentials",
                        responseFields(
                                fieldWithPath("error").description("Error type (e.g., 'Authentication Required')"),
                                fieldWithPath("message").description("Authentication error message (e.g., 'Authentication required.')")
                        )
                ));
    }

    @Test
    @DisplayName("Get Users - Wrong Credentials")
    void getUsersUnauthorizedDueToWrongCredentials() throws Exception {
        String wrongCredentials = "wronguser:wrongpass";
        String base64WrongCredentials = Base64.getEncoder().encodeToString(wrongCredentials.getBytes());

        mockMvc.perform(get("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + base64WrongCredentials))
                .andExpect(status().isUnauthorized())
                .andDo(document("get-users-wrong-credentials",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("Credentials for Basic Authentication (format: 'Basic base64(username:password))')")
                        ),
                        responseFields(
                                fieldWithPath("error").description("Error type (e.g., 'Authentication Failed')"),
                                fieldWithPath("message").description("Authentication error message (e.g., 'Invalid password!' or 'Invalid username or password!')")
                        )
                ));
    }
}