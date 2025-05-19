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
public class SetNotifyCata extends BotCommand {

    private final UserService userService;

    public SetNotifyCata(UserService userService) {
        super("alert", "Set/unset warning about cataclysms.");
        this.userService = userService;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] args) {
        long telegramId = user.getId();
        var dbUser = userService.getUser(telegramId);

        if (dbUser == null) {
            sendMessage(telegramClient, chat.getId(), "User not registered. Please register first.");
            return;
        }

        boolean currentValue = dbUser.isNotifyCataclysm();
        dbUser.setNotifyCataclysm(!currentValue);

        userService.save(dbUser);

        String status = dbUser.isNotifyCataclysm() ? "enabled" : "disabled";
        sendMessage(telegramClient, chat.getId(), "Cataclysm alerts have been " + status + ".");
    }

    private void sendMessage(TelegramClient client, long chatId, String text) {
        try {
            client.execute(new SendMessage(String.valueOf(chatId), text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
