package com.telegrambot.model;

import java.math.BigDecimal;
import java.sql.Time;

public class User {
    private long chatId;
    private String cityName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Time scheduleTime;
    private boolean notifyPrecipitation;
    private boolean notifyCataclysm;

    // Конструктор
    public User(long chatId, String cityName, BigDecimal latitude, BigDecimal longitude,
                Time scheduleTime, boolean notifyPrecipitation, boolean notifyCataclysm) {
        this.chatId = chatId;
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.scheduleTime = scheduleTime;
        this.notifyPrecipitation = notifyPrecipitation;
        this.notifyCataclysm = notifyCataclysm;
    }

    // Геттеры и сеттеры

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
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

    public boolean isNotifyPrecipitation() {
        return notifyPrecipitation;
    }

    public void setNotifyPrecipitation(boolean notifyPrecipitation) {
        this.notifyPrecipitation = notifyPrecipitation;
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
                "chatId=" + chatId +
                ", cityName='" + cityName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", scheduleTime=" + scheduleTime +
                ", notifyPrecipitation=" + notifyPrecipitation +
                ", notifyCataclysm=" + notifyCataclysm +
                '}';
    }
}
