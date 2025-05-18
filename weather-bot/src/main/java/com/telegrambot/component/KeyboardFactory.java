package com.telegrambot.component;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

@Component
public class KeyboardFactory {

    public InlineKeyboardMarkup createDaysSelectionKeyboard() {
        InlineKeyboardButton oneDay = new InlineKeyboardButton("Today");
        oneDay.setCallbackData("forecast_days_1");

        InlineKeyboardButton twoDays = new InlineKeyboardButton("Tomorrow");
        twoDays.setCallbackData("forecast_days_2");

        InlineKeyboardButton threeDays = new InlineKeyboardButton("3 days ahead");
        threeDays.setCallbackData("forecast_days_3");

        List<InlineKeyboardButton> row = List.of(oneDay, twoDays, threeDays);
        return new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(row)));
    }
}
