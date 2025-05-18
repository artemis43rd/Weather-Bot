package com.telegrambot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelegramBot extends CommandLongPollingTelegramBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final ConcurrentHashMap<Integer, Long> usersChats;
    private final TelegramClient client;
    private final Weather weatherCommand;

    public TelegramBot(TelegramClient client, Weather weatherCommand, @Value("${bot.name}") String botName) {
        super(client, true, () -> botName);
        this.client = client;
        this.weatherCommand = weatherCommand;
        usersChats = new ConcurrentHashMap<>();
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

        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage(update.getMessage().getChatId().toString(), "Я получил ваше сообщение!");
            try {
                client.execute(message);
            } catch (TelegramApiException e) {
                logger.error("Ошибка отправки сообщения", e);
            }
        }
    }
}
