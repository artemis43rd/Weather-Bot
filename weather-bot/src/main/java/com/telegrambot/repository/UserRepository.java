package com.telegrambot.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.telegrambot.model.User;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User findByTelegramId(long telegramId) {
        String sql = "SELECT * FROM users WHERE telegram_id = ?";
        var users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), telegramId);
        return users.isEmpty() ? null : users.get(0);
    }

    @Transactional
    public void save(User user) {
        String sql = "INSERT INTO users (telegram_id, city_name, latitude, longitude, " +
                    "schedule_time, notify_cataclysm) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (telegram_id) DO UPDATE SET " +
                    "city_name = EXCLUDED.city_name, " +
                    "latitude = EXCLUDED.latitude, " +
                    "longitude = EXCLUDED.longitude, " +
                    "schedule_time = EXCLUDED.schedule_time, " +
                    "notify_cataclysm = EXCLUDED.notify_cataclysm";

        jdbcTemplate.update(sql,
                user.getTelegramId(),
                user.getCityName(),
                user.getLatitude(),
                user.getLongitude(),
                user.getScheduleTime(),
                user.isNotifyCataclysm()
        );
    }
}
