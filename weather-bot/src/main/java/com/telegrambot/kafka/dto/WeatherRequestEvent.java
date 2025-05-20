package com.telegrambot.kafka.dto;

import java.util.Arrays;

public class WeatherRequestEvent {
    private Long userId;
    private Long chatId;
    private String[] args;

    public WeatherRequestEvent() {}

    public WeatherRequestEvent(Long userId, Long chatId, String[] args) {
        this.userId = userId;
        this.chatId = chatId;
        this.args = args;
    }

    // Геттеры
    public Long getUserId() { return userId; }
    public Long getChatId() { return chatId; }
    public String[] getArgs() { return args; }

    // Сеттеры
    public void setUserId(Long userId) { this.userId = userId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public void setArgs(String[] args) { this.args = args; }

    @Override
    public String toString() {
        return "WeatherRequestEvent{" +
                "userId=" + userId +
                ", chatId=" + chatId +
                ", args=" + (args == null ? null : Arrays.toString(args)) +
                '}';
    }
}