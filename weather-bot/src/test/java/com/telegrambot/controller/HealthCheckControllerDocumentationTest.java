package com.telegrambot.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.telegrambot.config.TestMockConfig;
import com.telegrambot.config.WebConfig;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ContextConfiguration(classes = {WebConfig.class, TestMockConfig.class})
@WebAppConfiguration
class HealthCheckControllerDocumentationTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
    mockMvc = MockMvcBuilders.standaloneSetup(new HealthCheckController())
        .apply(documentationConfiguration(restDocumentation))
        .build();
    }

    @Test
    void healthCheckExample() throws Exception {
        mockMvc.perform(get("/healthcheck"))
            .andExpect(status().isOk())
            .andDo(document("health-check"));
    }
}