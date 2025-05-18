package com.telegrambot.component;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class HelpCommand extends BotCommand {

    private final ICommandRegistry commandRegistry;

    public HelpCommand(ICommandRegistry commandRegistry) {
        super("help", "Get all the commands this bot provides.");
        this.commandRegistry = commandRegistry;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] args) {
        StringBuilder helpMessageBuilder = new StringBuilder("Help\n");
        helpMessageBuilder.append("Non-command interaction:\nYou can send your location to get weather for your live-location\n\n" +
                                    "These are the registered commands for this Bot:\n\n");

        for (IBotCommand botCommand : commandRegistry.getRegisteredCommands()) {
            helpMessageBuilder.append(botCommand.toString()).append("\n\n");
        }

        SendMessage helpMessage = new SendMessage(chat.getId().toString(), helpMessageBuilder.toString());
        helpMessage.enableHtml(true);

        try {
            telegramClient.execute(helpMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
