package com.telegrambot.component;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.telegrambot.service.UserService;

@Component
public class PrintSettings extends BotCommand {

    private final UserService userService;

    public PrintSettings(UserService userService) {
        super("settings", "Print your current settings.");
        this.userService = userService;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] args) {
        com.telegrambot.model.User settings = userService.getUser(user.getId());

        String messageText;
        if (settings == null) {
            messageText = "The settings were not found. Please register first.";
        } else {
            String locationInfo;
            if (settings.getCityName() != null && !settings.getCityName().isBlank()) {
                locationInfo = "City: " + settings.getCityName();
            } else if (settings.getLatitude() != null && settings.getLongitude() != null) {
                locationInfo = String.format("Coordinates: %.6f, %.6f",
                        settings.getLatitude(), settings.getLongitude());
            } else {
                locationInfo = "Location not specified";
            }
            messageText = String.format("""
                📍Your current settings:
                ────────────────
                🌎%s
                🕐Notification time: %s
                🌩️Cataclysms notifications: %s
                ────────────────
                """,
                locationInfo,
                settings.getScheduleTime() != null ? settings.getScheduleTime().toString() : "not set",
                settings.isNotifyCataclysm() ? "on" : "off"
            );
        }

        SendMessage message = new SendMessage(chat.getId().toString(), messageText);
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
