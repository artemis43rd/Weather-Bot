package com.telegrambot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegrambot.kafka.dto.WeatherRequestEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    public static final String WEATHER_REQUEST_TOPIC = "weather-requests";

    @Value("${kafka.bootstrap.servers:localhost:9092}")
    private String bootstrapServers;

    private KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        try {
            try (AdminClient adminClient = AdminClient.create(props)) {
                NewTopic newTopic = new NewTopic(WEATHER_REQUEST_TOPIC, 1, (short) 1);
                adminClient.createTopics(Collections.singleton(newTopic)).all().get(10, TimeUnit.SECONDS);
                logger.info("Topic {} checked/created successfully.", WEATHER_REQUEST_TOPIC);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof TopicExistsException) {
                    logger.info("Topic {} already exists.", WEATHER_REQUEST_TOPIC);
                } else {
                    logger.warn("Failed to create/check topic {}: {}", WEATHER_REQUEST_TOPIC, e.getMessage());
                }
            } catch (TimeoutException e) {
                logger.warn("Timeout while trying to create/check topic {}", WEATHER_REQUEST_TOPIC, e);
            }

            this.producer = new KafkaProducer<>(props);
            // Простая проверка соединения путем запроса метаданных (может заблокировать на короткое время)
            this.producer.partitionsFor(WEATHER_REQUEST_TOPIC);
            logger.info("KafkaProducer initialized and connected successfully for topic: {}. Bootstrap servers: {}", WEATHER_REQUEST_TOPIC, bootstrapServers);

        } catch (Exception e) {
            logger.error("Failed to initialize or connect KafkaProducer. Bootstrap servers: {}. Error: {}", bootstrapServers, e.getMessage(), e);
        }
    }

    public void sendWeatherRequest(WeatherRequestEvent event) {
        if (producer == null) {
            logger.error("KafkaProducer is not initialized or failed to connect. Cannot send message: {}", event);
            return;
        }
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            ProducerRecord<String, String> record = new ProducerRecord<>(WEATHER_REQUEST_TOPIC, String.valueOf(event.getUserId()), jsonMessage);
            
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("Weather request sent to Kafka: topic={}, partition={}, offset={}, key={}",
                            metadata.topic(), metadata.partition(), metadata.offset(), record.key());
                } else {
                    // Ошибка здесь означает, что Kafka не приняла сообщение даже после ретраев.
                    logger.error("Failed to send weather request to Kafka after retries. Event: {}. Error: {}", event.toString(), exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            logger.error("Error serializing or preparing weather request event for Kafka: {}. Error: {}", event.toString(), e.getMessage(), e);
        }
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            logger.info("Flushing and closing KafkaProducer (timeout 10s)...");
            producer.close(Duration.ofSeconds(10));
            logger.info("KafkaProducer closed.");
        }
    }
}