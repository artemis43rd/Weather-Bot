package com.telegrambot.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.telegrambot.config.TestMockConfig;
import com.telegrambot.config.WebConfig;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@ContextConfiguration(classes = {WebConfig.class, TestMockConfig.class})
class AdminControllerDocumentationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

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
    void getUsersExample() throws Exception {
        mockMvc.perform(get("/admin/users?password=admin"))
                .andExpect(status().isOk())
                .andDo(document("get-users",
                        queryParameters(
                                parameterWithName("password")
                                        .description("Пароль администратора")
                        ),
                        responseFields(
                                fieldWithPath("[].telegramId")
                                        .description("ID пользователя в Telegram"),
                                fieldWithPath("[].cityName")
                                        .description("Город пользователя"),
                                fieldWithPath("[].latitude")
                                        .description("Широта"),
                                fieldWithPath("[].longitude")
                                        .description("Долгота"),
                                fieldWithPath("[].scheduleTime")
                                        .description("Время уведомления"),
                                fieldWithPath("[].notifyCataclysm")
                                        .description("Уведомление о катаклизмах")
                        )));
    }
}
