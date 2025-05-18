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
public class StartCommand extends BotCommand {

    private final UserService userService;

    public StartCommand(UserService userService) {
        super("start", "Register in the bot.");
        this.userService = userService;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        // Возвращает true, если пользователь новый, иначе false
        boolean isNewUser = signUp(user);

        StringBuilder builder = new StringBuilder();
        if (isNewUser) {
            builder.append("Welcome ").append(user.getFirstName()).append(" to Weather Bot!\nYou have been registered as: ").append(user.getUserName());
        } else {
            builder.append("Welcome back, ").append(user.getFirstName()).append("! You are already registered.");
        }

        try {
            telegramClient.execute(new SendMessage(chat.getId().toString(), builder.toString()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean signUp(User user) {
        // Проверяем, существует ли пользователь
        if (userService.getUser(user.getId()) == null) {
            // Если пользователя нет, создаем его
            userService.create(user.getId());
            System.out.println("New user created with ID: " + user.getId());
            return true; // Новый пользователь
        }
        return false; // Пользователь уже существует
    }
}
