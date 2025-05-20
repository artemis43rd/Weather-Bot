package com.telegrambot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

record HealthStatusRecord(String status, List<String> authors, String message) {}
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