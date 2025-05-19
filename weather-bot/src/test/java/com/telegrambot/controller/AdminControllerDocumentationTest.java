package com.telegrambot.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.telegrambot.config.TestMockConfig;
import com.telegrambot.config.WebConfig;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@ContextConfiguration(classes = {WebConfig.class, TestMockConfig.class})
public class AdminControllerDocumentationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    //@Test
    public void getUsersExample() throws Exception {
        mockMvc.perform(get("/admin/users").param("password", "admin"))
                .andExpect(status().isOk())
                .andDo(document("get-users",
                        RequestDocumentation.requestParameters(
                                RequestDocumentation.parameterWithName("password").description("Пароль администратора")
                        ),
                        PayloadDocumentation.responseFields(
                                PayloadDocumentation.fieldWithPath("[].telegramId").description("ID пользователя в Telegram"),
                                PayloadDocumentation.fieldWithPath("[].cityName").description("Город пользователя"),
                                PayloadDocumentation.fieldWithPath("[].latitude").description("Широта"),
                                PayloadDocumentation.fieldWithPath("[].longitude").description("Долгота"),
                                PayloadDocumentation.fieldWithPath("[].scheduleTime").description("Время уведомления"),
                                PayloadDocumentation.fieldWithPath("[].notifyCataclysm").description("Уведомление о катаклизмах")
                        )));
    }
}
