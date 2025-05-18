package com.telegrambot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelegramBot extends CommandLongPollingTelegramBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final ConcurrentHashMap<Integer, Long> usersChats;
    private final TelegramClient client;

    public TelegramBot(TelegramClient client, @Value("${bot.name}") String botName) {
        super(client, true, () -> botName);
        this.client = client;
        usersChats = new ConcurrentHashMap<>();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        SendMessage message = new SendMessage(update.getMessage().getChatId().toString(), "Я получил ваше сообщение!");
        try {
            client.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Ошибка отправки сообщения", e);
        }
    }
}
