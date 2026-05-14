package com.telegrambot;

import java.io.File;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.core.StandardContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.telegram.telegrambots.longpolling.BotSession;
import com.telegrambot.config.DatabaseConfig;
import com.telegrambot.config.TelegramConfig;
import com.telegrambot.config.WebConfig;

public class Main {
    public static void main(String[] args) throws Exception {

        AnnotationConfigApplicationContext botContext = new AnnotationConfigApplicationContext();
        botContext.register(DatabaseConfig.class, TelegramConfig.class);
        botContext.refresh();
        Runtime.getRuntime().addShutdownHook(new Thread(botContext::close));

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        String docBase = new File(".").getAbsolutePath();
        StandardContext ctx = (StandardContext) tomcat.addContext("", docBase);

        AnnotationConfigWebApplicationContext restContext = new AnnotationConfigWebApplicationContext();
        restContext.setParent(botContext);
        restContext.setServletContext(ctx.getServletContext());
        restContext.register(WebConfig.class);

        String servletName = "dispatcher";
        DispatcherServlet dispatcher = new DispatcherServlet(restContext);
        Tomcat.addServlet(ctx, servletName, dispatcher);
        ctx.addServletMappingDecoded("/", servletName);

        restContext.refresh();

        tomcat.start();
        System.out.println("Tomcat started on port 8080. Healthcheck is available!");

        Thread botThread = new Thread(() -> {
            try {
                System.out.println("Attempting to start Telegram bot...");
                botContext.getBean(BotSession.class);
                System.out.println("Telegram bot started successfully!");
            } catch (Exception e) {
                System.err.println("!!! Telegram bot failed to start (Network issue) !!!");
                System.err.println("!!! But don't worry, Web Server remains ACTIVE. !!!");
            }
        });
        botThread.start();

        Thread.currentThread().join();
    }
}
