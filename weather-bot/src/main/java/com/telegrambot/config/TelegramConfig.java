package com.telegrambot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.telegrambot.component.*;

@Configuration
@PropertySource("classpath:bot.properties")
@ComponentScan(basePackages = {
        "com.telegrambot.component",   // команды бота
        "com.telegrambot.service",
        "com.telegrambot.repository",
        "com.telegrambot.model"        // сущности JPA/JDBC
})
public class TelegramConfig {

    @Autowired
    private Environment env;

    /* команды Telegram-бота */
    @Autowired private TelegramBot   bot;
    @Autowired private StartCommand  start;
    @Autowired private HelpCommand   help;
    @Autowired private SetLocation   setLocation;
    @Autowired private PrintSettings settings;
    @Autowired private Weather       weather;
    @Autowired private SetSchedule   schedule;
    @Autowired private SetNotifyCata alert;

    /* ----- инициализация бота ----- */
    @Bean
    public BotSession sessionStart(TelegramBotsLongPollingApplication app) throws TelegramApiException {
        bot.register(start);
        bot.register(help);
        bot.register(setLocation);
        bot.register(settings);
        bot.register(weather);
        bot.register(schedule);
        bot.register(alert);
        return app.registerBot(env.getProperty("token"), bot);
    }

    @Bean
    public TelegramBotsLongPollingApplication application() {
        return new TelegramBotsLongPollingApplication();
    }

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(env.getProperty("token"));
    }
}
