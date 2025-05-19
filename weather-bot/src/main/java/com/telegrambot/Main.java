package com.telegrambot;

import org.apache.catalina.startup.Tomcat;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.telegram.telegrambots.longpolling.BotSession;
import com.telegrambot.config.DatabaseConfig;
import com.telegrambot.config.TelegramConfig;
import com.telegrambot.config.WebConfig;

import jakarta.servlet.ServletRegistration;

public class Main {

    public static void main(String[] args) throws Exception {
        // Контекст для Telegram-бота и базы
        AnnotationConfigApplicationContext botContext = new AnnotationConfigApplicationContext(
                DatabaseConfig.class, TelegramConfig.class
        );

        Runtime.getRuntime().addShutdownHook(new Thread(botContext::close));

        // Запускаем Telegram-бота (инициализация BotSession)
        botContext.getBean(BotSession.class);

        // Встроенный Tomcat
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        // Создаём контекст Tomcat с веб-контекстом
        var context = tomcat.addContext("", null);

        // Создаём контекст Spring MVC после создания Tomcat и контекста
        AnnotationConfigWebApplicationContext restContext = new AnnotationConfigWebApplicationContext();
        restContext.register(WebConfig.class);
        restContext.setServletContext(context.getServletContext());
        restContext.refresh();

        // Создаём и регистрируем DispatcherServlet
        DispatcherServlet dispatcherServlet = new DispatcherServlet(restContext);
        ServletRegistration.Dynamic servlet = context.getServletContext()
                .addServlet("dispatcher", dispatcherServlet);
        servlet.setLoadOnStartup(1);
        servlet.addMapping("/");

        // Запускаем Tomcat
        tomcat.start();

        // Ждём в главном потоке, чтобы приложение не завершилось
        Thread.currentThread().join();
    }
}