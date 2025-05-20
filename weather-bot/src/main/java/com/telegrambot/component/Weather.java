package com.telegrambot.component;

import com.telegrambot.kafka.dto.WeatherRequestEvent;
import com.telegrambot.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class Weather extends BotCommand {
    private static final Logger logger = LoggerFactory.getLogger(Weather.class);

    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public Weather(KafkaProducerService kafkaProducerService) {
        super("weather", "Print weather forecast for your location in settings.");
        this.kafkaProducerService = kafkaProducerService;
    }

     private boolean isCityNameArgument(String arg) {
        return arg != null && !arg.isBlank() &&
                arg.matches("[\\p{L} \\-]+") &&
                !arg.matches("\\d+");
    }

    private boolean isNumericArgument(String arg) {
        if (arg == null || arg.isBlank()) {
            return false;
        }
        try {
            Integer.parseInt(arg);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void execute(TelegramClient client, User user, Chat chat, String[] args) {
        Long userId = user.getId();
        Long chatId = chat.getId();

        boolean sendConfirmation = false;
        if (args.length == 1) {
            if (isNumericArgument(args[0])) {
                sendConfirmation = true;
            }
        } else if (args.length == 2) {
            if (isCityNameArgument(args[0]) && isNumericArgument(args[1])) {
                sendConfirmation = true;
            }
        }

        if (sendConfirmation) {
            SendMessage confirmationMessage = new SendMessage(
                    chat.getId().toString(),
                    "Your weather request has been received and is being processed. Please wait..."
            );
            try {
                Message sentMessage = client.execute(confirmationMessage);
            } catch (TelegramApiException e) {
                logger.error("Failed to send weather request confirmation to user {} (chat {})", userId, chatId, e);
            }
        }

        WeatherRequestEvent weatherRequest = new WeatherRequestEvent(userId, chatId, args);
        kafkaProducerService.sendWeatherRequest(weatherRequest);
    }
}