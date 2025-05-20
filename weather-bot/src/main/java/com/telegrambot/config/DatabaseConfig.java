package com.telegrambot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@PropertySource("classpath:database.properties")
public class DatabaseConfig {

    @Value("${db.driver}")
    private String driverClassName;

    @Value("${db.url}")
    private String url;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    @Value("${db.pool.initialSize}")
    private int initialSize;

    @Value("${db.pool.maxTotal}")
    private int maxTotal;

    @Value("${db.pool.minIdle}")
    private int minIdle;

    @Value("${db.pool.maxIdle}")
    private int maxIdle;

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // Настройки пула соединений
        dataSource.setInitialSize(initialSize);
        dataSource.setMaxTotal(maxTotal);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxIdle(maxIdle);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    // Выполнение скрипта SQL для создания таблицы
    @PostConstruct
    public void initDatabase() throws IOException {
        ClassPathResource resource = new ClassPathResource("create.sql");
        byte[] fileData = FileCopyUtils.copyToByteArray(resource.getInputStream());
        String createTableSql = new String(fileData, StandardCharsets.UTF_8);
        jdbcTemplate().execute(createTableSql);
    }
}
