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
public class SetSchedule extends BotCommand {

    private final UserService userService;

    public SetSchedule(UserService userService) {
        super("schedule", "Set the weather departure time for the day.");
        this.userService = userService;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] args) {
        var dbUser = userService.getUser(user.getId());
        if (dbUser == null) {
            sendMessage(telegramClient, chat.getId(), "The settings were not found. Please register.");
            return;
        }

        if (args.length == 0) {
            sendMessage(telegramClient, chat.getId(), "Please specify the time in the HH:mm format, for example: /schedule 08:30");
            return;
        }

        String timeStr = args[0];
            try {
                java.sql.Time scheduleTime = java.sql.Time.valueOf(timeStr + ":00"); // add sec
                userService.updateScheduleTime(user.getId(), scheduleTime);
                sendMessage(telegramClient, chat.getId(), "The forecast sending time has been successfully set to " + timeStr);
            } catch (IllegalArgumentException e) {
                sendMessage(telegramClient, chat.getId(), "Incorrect time format. Use HH:mm, for example: 08:30");
            }
        }

    private void sendMessage(TelegramClient client, Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            client.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
