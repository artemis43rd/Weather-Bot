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
@ComponentScan(basePackages = "com.telegrambot")
public class TelegramConfig {

    @Autowired
    Environment env;

    @Autowired
    TelegramBot bot;

    @Autowired
    StartCommand start;

    @Autowired
    SetLocation setLocation;

    @Autowired
    PrintSettings settings;

    @Autowired
    Weather weather;

    @Autowired
    SetSchedule schedule;

    @Autowired
    HelpCommand help;

    @Bean
    public BotSession sessionStart(TelegramBotsLongPollingApplication botsApplication, TelegramBot bot) throws TelegramApiException {
        bot.register(start);
        bot.register(help);
        bot.register(setLocation);
        bot.register(settings);
        bot.register(weather);
        bot.register(schedule);
        return botsApplication.registerBot(env.getProperty("token"), bot);
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
