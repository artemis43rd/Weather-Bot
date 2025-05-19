package com.telegrambot.service;

import java.sql.Time;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.telegrambot.model.User;
import com.telegrambot.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User create(long telegramId) {
        User user = new User();
        user.setTelegramId(telegramId);
        userRepository.save(user);
        return user;
    }

    public User getUser(long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void updateScheduleTime(long telegramId, Time scheduleTime) {
        User user = getUser(telegramId);
        if (user != null) {
            user.setScheduleTime(scheduleTime);
            userRepository.updateScheduleTime(telegramId, scheduleTime);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }
}
