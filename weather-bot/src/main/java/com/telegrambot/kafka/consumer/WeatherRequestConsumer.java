package com.telegrambot.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegrambot.kafka.dto.WeatherRequestEvent;
import com.telegrambot.service.KafkaProducerService;
import com.telegrambot.service.WeatherService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@Component
public class WeatherRequestConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WeatherRequestConsumer.class);

    @Value("${kafka.bootstrap.servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.consumer.group.id:weather-consumer-group}")
    private String groupId;

    private final WeatherService weatherService;
    private final TelegramClient telegramClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private KafkaConsumer<String, String> consumer;
    private ExecutorService executorService;

    @Autowired
    public WeatherRequestConsumer(WeatherService weatherService, TelegramClient telegramClient) {
        this.weatherService = weatherService;
        this.telegramClient = telegramClient;
    }

    @PostConstruct
    public void startConsumer() {
        if (running.get()) {
            logger.warn("WeatherRequestConsumer is already running.");
            return;
        }

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        try {
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList(KafkaProducerService.WEATHER_REQUEST_TOPIC));
            logger.info("KafkaConsumer subscribed to topic: {} with group ID: {}", KafkaProducerService.WEATHER_REQUEST_TOPIC, groupId);

            executorService = Executors.newSingleThreadExecutor(r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setName("WeatherRequestConsumerThread");
                return t;
            });
            running.set(true);
            executorService.submit(this);
            logger.info("WeatherRequestConsumer started in a new thread.");
        } catch (Exception e) {
            logger.error("Failed to initialize or start KafkaConsumer", e);
            running.set(false);
        }
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                    if (records.isEmpty() && !running.get()) { // Проверка для быстрого выхода при остановке
                        break;
                    }
                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Received message: key={}, value(preview)={}, partition={}, offset={}",
                                record.key(), record.value().substring(0, Math.min(record.value().length(), 100)), record.partition(), record.offset());
                        try {
                            WeatherRequestEvent event = objectMapper.readValue(record.value(), WeatherRequestEvent.class);
                            logger.debug("Processing event: {}", event);
                            weatherService.handleWeatherCommand(telegramClient, event.getUserId(), event.getChatId(), event.getArgs());
                        } catch (Exception e) {
                            logger.error("Error processing weather request event from Kafka. Record: key={}, value={}", record.key(), record.value(), e);
                        }
                    }
                } catch (WakeupException e) {
                    logger.info("KafkaConsumer wakeup signal received, exiting poll loop.");
                    break;
                } catch (Exception e) { // Ловим другие возможные исключения из poll()
                    logger.error("Error in Kafka consumer poll loop", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break; // Выходим, если поток прерван
                    }
                }
            }
        } finally {
            if (consumer != null) {
                consumer.close();
            }
            running.set(false); // Устанавливаем в false, если вышли из цикла
            logger.info("KafkaConsumer processing loop finished and consumer closed.");
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down WeatherRequestConsumer...");
        running.set(false); // Устанавливаем флаг для остановки цикла
        if (consumer != null) {
            consumer.wakeup(); // Сигнал для выхода из poll()
        }
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) { // Даем больше времени на завершение
                    logger.warn("Consumer thread did not terminate in time, forcing shutdown.");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for consumer thread to terminate.");
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("WeatherRequestConsumer shut down complete.");
    }
}