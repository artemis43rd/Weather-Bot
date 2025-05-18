package com.telegrambot.component;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.telegrambot.service.UserService;

@Component
public class SetLocation extends BotCommand {

    private final UserService userService;

    public SetLocation(UserService userService) {
        super("setlocation", "set your location for the weather.");
        this.userService = userService;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] args) {
        SendMessage message = new SendMessage(chat.getId().toString(), "");

        if (args.length == 0) {
            message.setText("Please specify the city or coordinates (latitude and longitude).");
        } else if (args.length == 2) {
            try {
                BigDecimal lat = new BigDecimal(args[0]);
                BigDecimal lon = new BigDecimal(args[1]);

                if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0 ||
                    lon.compareTo(BigDecimal.valueOf(-180)) < 0 || lon.compareTo(BigDecimal.valueOf(180)) > 0) {
                    message.setText("Error: coordinates are out of the acceptable range.");
                } else {
                    com.telegrambot.model.User u = userService.getUser(user.getId());
                    u.setLatitude(lat);
                    u.setLongitude(lon);
                    u.setCityName(null);
                    userService.save(u);
                    message.setText("Coordinates have been updated successfully.");
                }
            } catch (NumberFormatException e) {
                message.setText("Error: coordinates must be numbers.");
            }
        } else {
            String cityName = String.join(" ", args);
            com.telegrambot.model.User u = userService.getUser(user.getId());
            u.setCityName(cityName);
            u.setLatitude(null);
            u.setLongitude(null);
            userService.save(u);
            message.setText("The city is set: " + cityName);
        }

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
