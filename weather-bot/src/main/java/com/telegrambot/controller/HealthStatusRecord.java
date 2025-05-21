// Файл: ./weather-bot/src/main/java/com/telegrambot/controller/HealthStatusRecord.java
package com.telegrambot.controller;

import java.util.List;

public record HealthStatusRecord(String status, List<String> authors, String message) {}