package com.telegrambot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.telegrambot.service.GeocodingService;
import com.telegrambot.service.UserService;
import com.telegrambot.service.WeatherService;

@Component
@PropertySource("classpath:bot.properties")
public class TelegramBot extends CommandLongPollingTelegramBot {

    @Value("${api-key}")
    private String apiKey;

    @Autowired
    private WeatherService weatherService;

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final ConcurrentHashMap<Integer, Long> usersChats;
    private final TelegramClient client;
    private final Weather weatherCommand;
    private final ScheduledExecutorService scheduler;

    private final UserService userService;

    public TelegramBot(TelegramClient client, Weather weatherCommand,
            @Value("${bot.name}") String botName, UserService userService) {
        super(client, true, () -> botName);

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::backGroundTask, 0, 60, TimeUnit.SECONDS);

        this.client = client;
        this.weatherCommand = weatherCommand;
        usersChats = new ConcurrentHashMap<>();

        this.userService = userService;
    }

    private void backGroundTask() {
        checkWeatherCataclysms();
        sendDailyScheduled();
    }

    private void checkWeatherCataclysms() {
        List<com.telegrambot.model.User> users = userService.getAllUsers();
        for (com.telegrambot.model.User user : users) {
            Long chatId = user.getTelegramId();
            try {
                weatherService.checkDailyWeatherAndNotify(client, chatId);
            } catch (Exception e) {
                logger.error("Error checking weather for user " + chatId, e);
            }
        }
    }

    public void sendDailyScheduled() {
        List< com.telegrambot.model.User> users = userService.getAllUsers();
        LocalTime now = LocalTime.now();

        for (com.telegrambot.model.User user : users) {
            Time scheduleTime = user.getScheduleTime();
            if (scheduleTime == null) continue;

            LocalTime userTime = scheduleTime.toLocalTime();
            if (now.getHour() == userTime.getHour() && now.getMinute() == userTime.getMinute()) {
                Long chatId = user.getTelegramId();
                sendResponse(chatId, "Your scheduled forecast for today.");
                weatherService.handleWeatherCommand(client, chatId, chatId, new String[]{"1"});
            }
        }
    }

    @Override
    public boolean filter(Message message) {
        usersChats.put(message.getFrom().getId().intValue(), message.getChatId());
        return super.filter(message);
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            var callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            User user = callbackQuery.getFrom();
            Chat chat = callbackQuery.getMessage().getChat();
            int messageId = callbackQuery.getMessage().getMessageId();

            DeleteMessage deleteMessage = DeleteMessage.builder()
                    .chatId(String.valueOf(chat.getId()))
                    .messageId(messageId)
                    .build();
            try {
                client.execute(deleteMessage);
            } catch (TelegramApiException e) {
                logger.error("Error deleting a message", e);
            }

            if (data.startsWith("weather_days_")) {
                String[] parts = data.split("_", 4);
                String city = parts[2];
                String daysStr = parts[3];
                int days;
                try {
                    days = Integer.parseInt(daysStr);
                } catch (NumberFormatException e) {
                    days = 1; // default 1
                }
                weatherCommand.execute(client, user, chat, new String[]{city, String.valueOf(days)});
            }

            if (data.startsWith("forecast_days_")) {
                int days;
                try {
                    days = Integer.parseInt(data.substring("forecast_days_".length()));
                } catch (NumberFormatException e) {
                    days = 1; // default 1
                }

                weatherCommand.execute(client, user, chat, new String[]{String.valueOf(days)});
            }
            return;
        }

        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();

            if (update.getMessage().hasLocation()) {
                User user = update.getMessage().getFrom();
                Chat chat = update.getMessage().getChat();

                // Geoposition
                double latitude = update.getMessage().getLocation().getLatitude();
                double longitude = update.getMessage().getLocation().getLongitude();
                handleCoordinates(client, user, chat, latitude, longitude);
            }

            if (update.getMessage().hasText()) {
                sendResponse(chatId, "I am a simple bot that show the weather." +
                    "I don't reply to messages unless they are commands.\n\n" +
                    "If you want to know what I can do, write to /help.");
            }
        }
    }

    private void handleCoordinates(TelegramClient client, User user, Chat chat, double latitude, double longitude) {
        try {
            GeocodingService geocodingService = new GeocodingService(apiKey);
            String cityName = geocodingService.getCityName(latitude, longitude);
            System.out.println("cityName " + cityName);
            if (cityName != null && !cityName.isBlank()) {
                weatherCommand.execute(client, user, chat, new String[]{cityName});
            } else {
                sendResponse(chat.getId(), "City name not found for the given coordinates.");
            }
        } catch (Exception e) {
            sendResponse(chat.getId(), "Error processing location data. Please try again.");
        }
    }

    private void sendResponse(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            client.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending message: " + e.getMessage(), e);
        }
    }
}
