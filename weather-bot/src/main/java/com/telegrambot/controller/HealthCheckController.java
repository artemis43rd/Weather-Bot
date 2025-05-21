// Файл: ./weather-bot/src/main/java/com/telegrambot/controller/HealthCheckController.java
package com.telegrambot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List; // Убедись, что импорт есть, если его не было

@RestController
public class HealthCheckController {

    @GetMapping("/healthcheck")
    public HealthStatusRecord healthcheck() {
        return new HealthStatusRecord(
                "Server is running!!!!",
                List.of("Cherdantsev", "Zoloev", "Romashko"),
                "System is healthy"
        );
    }
}