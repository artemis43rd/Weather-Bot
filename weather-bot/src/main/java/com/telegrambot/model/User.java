package com.telegrambot.model;

import java.math.BigDecimal;
import java.sql.Time;

public class User {
    private long telegram_id;
    private String cityName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Time scheduleTime;
    private boolean notifyCataclysm;

    // Конструкторы
    public User(long telegram_id, String cityName, BigDecimal latitude, BigDecimal longitude,
                Time scheduleTime, boolean notifyCataclysm) {
        this.telegram_id = telegram_id;
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.scheduleTime = scheduleTime;
        this.notifyCataclysm = notifyCataclysm;
    }

    public User() {
        this.notifyCataclysm = false;
    }

    // Геттеры и сеттеры

    public long getTelegramId() {
        return telegram_id;
    }

    public void setTelegramId(long telegram_id) {
        this.telegram_id = telegram_id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Time getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Time scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public boolean isNotifyCataclysm() {
        return notifyCataclysm;
    }

    public void setNotifyCataclysm(boolean notifyCataclysm) {
        this.notifyCataclysm = notifyCataclysm;
    }

    @Override
    public String toString() {
        return "User{" +
                "telegram_id=" + telegram_id +
                ", cityName='" + cityName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", scheduleTime=" + scheduleTime +
                ", notifyCataclysm=" + notifyCataclysm +
                '}';
    }
}
