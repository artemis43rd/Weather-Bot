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

        /* ───── контекст Telegram-бота и базы ───── */
        AnnotationConfigApplicationContext botContext = new AnnotationConfigApplicationContext();
        botContext.register(DatabaseConfig.class, TelegramConfig.class);
        botContext.refresh();
        Runtime.getRuntime().addShutdownHook(new Thread(botContext::close));

        botContext.getBean(BotSession.class);            // запуск бота

        /* ───── Embedded Tomcat + Spring MVC ───── */
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();                           // создаём коннектор

        String docBase = new File(".").getAbsolutePath();
        StandardContext ctx = (StandardContext) tomcat.addContext("", docBase);

        // web-контекст Spring MVC
        AnnotationConfigWebApplicationContext restContext = new AnnotationConfigWebApplicationContext();
        restContext.setParent(botContext);               // чтобы видеть сервисы
        restContext.setServletContext(ctx.getServletContext());
        restContext.register(WebConfig.class);

        String servletName = "dispatcher";
        DispatcherServlet dispatcher = new DispatcherServlet(restContext);
        Tomcat.addServlet(ctx, servletName, dispatcher);
        ctx.addServletMappingDecoded("/", servletName);

        restContext.refresh();                           // запускаем Spring MVC
        tomcat.start();                                  // стартуем Tomcat

        Thread.currentThread().join();                   // не даём процессу завершиться
    }
}
