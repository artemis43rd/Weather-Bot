package com.telegrambot.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import com.telegrambot.controller.AdminController;
import com.telegrambot.controller.HealthCheckController;
import com.telegrambot.service.AdminService;
import com.telegrambot.model.User;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

@Configuration
@Import(WebConfig.class)
public class TestMockConfig {

    @Bean
    @Primary
    public AdminService adminService() {
        AdminService mockService = Mockito.mock(AdminService.class);

        Mockito.when(mockService.getUsersIfAdmin(Mockito.anyString()))
       .thenReturn(List.of(
                new User(1122334455, "Moscow",
                    BigDecimal.valueOf(55.7558), BigDecimal.valueOf(37.6173),
                    Time.valueOf(LocalTime.of(9, 0)), true),
                new User(66778899, "Saint Petersburg",
                    BigDecimal.valueOf(59.9343), BigDecimal.valueOf(30.3351),
                    Time.valueOf(LocalTime.of(18, 30)), false)
            ));
        return mockService;
    }

    @Bean
    public AdminController adminController() {
        return new AdminController(adminService());
    }

    @Bean
    public HealthCheckController healthCheckController() {
        return new HealthCheckController();
    }

}
