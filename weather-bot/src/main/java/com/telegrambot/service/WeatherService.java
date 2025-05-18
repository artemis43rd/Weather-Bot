package com.telegrambot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

import com.telegrambot.component.KeyboardFactory;

@Component
public class WeatherService {

    @Value("${api-key}")
    private String apiKey;

    private final UserService userService;
    private final KeyboardFactory keyboardFactory;

    public WeatherService(UserService userService, KeyboardFactory keyboardFactory) {
        this.userService = userService;
        this.keyboardFactory = keyboardFactory;
    }

    public void handleWeatherCommand(TelegramClient client, User user, Chat chat, String[] args) {
        if (args.length == 0) {
            sendDaysSelectionMessage(client, chat.getId());
            return;
        }

        int forecastDays;
        try {
            forecastDays = Integer.parseInt(args[0]);
            if (forecastDays < 1 || forecastDays > 3) forecastDays = 3;
        } catch (Exception e) {
            forecastDays = 3;
        }

        var dbUser = userService.getUser(user.getId());
        if (dbUser == null) {
            sendMessage(client, chat.getId(), "The settings were not found. Please register.");
            return;
        }

        String url;
        if (dbUser.getCityName() != null && !dbUser.getCityName().isBlank()) {
            url = String.format(
                    "https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric&lang=eng",
                    dbUser.getCityName(), apiKey);
        } else if (dbUser.getLatitude() != null && dbUser.getLongitude() != null) {
            url = String.format(
                    "https://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&appid=%s&units=metric&lang=eng",
                    dbUser.getLatitude(), dbUser.getLongitude(), apiKey);
        } else {
            sendMessage(client, chat.getId(), "You don't have a city set or coordinates set.");
            return;
        }

        try {
            var response = new RestTemplate().getForObject(url, String.class);
            var json = new ObjectMapper().readTree(response);
            var forecastList = json.get("list");

            Map<String, Map<String, String>> dailyForecast = new LinkedHashMap<>();

            for (JsonNode item : forecastList) {
                String dtTxt = item.get("dt_txt").asText();
                LocalDate unformDate = LocalDate.parse(dtTxt.substring(0, 10));
                String hour = dtTxt.substring(11, 13);

                String date = String.format("%d %s (%s)",
                        unformDate.getDayOfMonth(),
                        unformDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        unformDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                );

                String partOfDay;
                int h = Integer.parseInt(hour);
                if (h >= 6 && h < 12) partOfDay = "🌅Morning";
                else if (h >= 12 && h < 18) partOfDay = "🌆Afternoon";
                else if (h >= 18 && h < 24) partOfDay = "🏙Evening";
                else continue;

                String desc = item.get("weather").get(0).get("description").asText();
                double temp = item.get("main").get("temp").asDouble();
                String info = String.format("%s | %.1f°C", desc, temp);

                dailyForecast.computeIfAbsent(date, k -> new LinkedHashMap<>()).put(partOfDay, info);
            }

            String message = formatForecastMessage(dailyForecast, forecastDays);
            sendMessage(client, chat.getId(), message);

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(client, chat.getId(), "Error receiving the weather forecast.");
        }
    }

    private void sendDaysSelectionMessage(TelegramClient client, long chatId) {
        InlineKeyboardMarkup markup = keyboardFactory.createDaysSelectionKeyboard();
        String messageText = "Select the number of forecast days:";
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), messageText);
        sendMessage.setReplyMarkup(markup);

        try {
            client.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatForecastMessage(Map<String, Map<String, String>> dailyForecast, int days) {
        StringBuilder msg = new StringBuilder("Weather forecast:\n\n");
        int count = 0;
        int skip = 0;
        if (days == 2) { skip = 1; days = 1; }

        int i = 0;
        for (var entry : dailyForecast.entrySet()) {
            if (i++ < skip) continue;
            if (count++ >= days) break;
            msg.append(entry.getKey()).append(":\n");
            for (var part : List.of("🌅Morning", "🌆Afternoon", "🏙Evening")) {
                var info = entry.getValue().get(part);
                if (info != null) msg.append(part).append(": ").append(info).append("\n");
            }
            msg.append("\n");
        }
        return msg.toString();
    }

    private void sendMessage(TelegramClient client, long chatId, String text) {
        try {
            client.execute(new SendMessage(String.valueOf(chatId), text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
