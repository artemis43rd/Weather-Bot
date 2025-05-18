package com.telegrambot;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.telegram.telegrambots.longpolling.BotSession;
import com.telegrambot.config.DatabaseConfig;
import com.telegrambot.config.TelegramConfig;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                DatabaseConfig.class, TelegramConfig.class
        );

        Runtime.getRuntime().addShutdownHook(new Thread(context::close));

        try {
            context.getBean(BotSession.class);
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread was interrupted.");
        } catch (Exception e) {
            System.err.println("Error during application startup: " + e.getMessage());
        }
    }
}
