CREATE TABLE IF NOT EXISTS users (
    chat_id BIGINT PRIMARY KEY,                            -- Уникальный идентификатор чата/пользователя Telegram
    city_name VARCHAR(255),                                -- Название города (может быть NULL)
    latitude DECIMAL(10, 8),                               -- Широта (может быть NULL)
    longitude DECIMAL(11, 8),                              -- Долгота (может быть NULL)
    schedule_time TIME,                                    -- Время для обновлений погоды (может быть NULL)
    notify_precipitation BOOLEAN DEFAULT false,            -- Оповещения о осадках, по умолчанию false
    notify_cataclysm BOOLEAN DEFAULT false,                -- Оповещения о катаклизмах, по умолчанию false
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Время создания записи, по умолчанию текущее время
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  -- Время последнего обновления, по умолчанию текущее время
);