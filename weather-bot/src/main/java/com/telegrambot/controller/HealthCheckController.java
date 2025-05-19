package com.telegrambot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "Server is running!!!!\n1)Cherdantsev\n2)Zoloev\n3)Romashko";
    }
}
