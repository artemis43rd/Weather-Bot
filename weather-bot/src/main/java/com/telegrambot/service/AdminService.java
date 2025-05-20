package com.telegrambot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.telegrambot.model.User;
import com.telegrambot.repository.UserRepository;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;

    @Value("${admin.password}")
    private String adminPassword;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsersIfAdmin(String password) {
        if (!adminPassword.equals(password)) {
            throw new SecurityException("Unauthorized");
        }
        return userRepository.getAllUsers();
    }
}
