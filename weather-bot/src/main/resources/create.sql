CREATE TABLE IF NOT EXISTS users (
    telegram_id BIGINT PRIMARY KEY,                        -- Уникальный идентификатор пользователя Telegram
    city_name VARCHAR(255),                                -- Название города (может быть NULL)
    latitude DECIMAL(10, 8),                               -- Широта (может быть NULL)
    longitude DECIMAL(11, 8),                              -- Долгота (может быть NULL)
    schedule_time TIME,                                    -- Время для обновлений погоды (может быть NULL)
    notify_cataclysm BOOLEAN DEFAULT false                -- Оповещения о катаклизмах, по умолчанию false
);