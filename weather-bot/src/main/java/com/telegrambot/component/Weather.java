package com.telegrambot.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.telegrambot.service.WeatherService;

@Component
public class Weather extends BotCommand {

    private final WeatherService weatherService;

    @Autowired
    public Weather(WeatherService weatherService) {
        super("weather", "Print weather forecast for your location in settings.");
        this.weatherService = weatherService;
    }

    @Override
    public void execute(TelegramClient client, User user, Chat chat, String[] args) {
        weatherService.handleWeatherCommand(client, user, chat, args);
    }
}
