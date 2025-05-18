package com.telegrambot.model;

import java.math.BigDecimal;
import java.sql.Time;

public class User {
    private long telegram_id;
    private String cityName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Time scheduleTime;
    private boolean notifyPrecipitation;
    private boolean notifyCataclysm;
    private Time time_notify;

    // Конструкторы
    public User(long telegram_id, String cityName, BigDecimal latitude, BigDecimal longitude,
                Time scheduleTime, boolean notifyPrecipitation, boolean notifyCataclysm, Time time_notify) {
        this.telegram_id = telegram_id;
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.scheduleTime = scheduleTime;
        this.notifyPrecipitation = notifyPrecipitation;
        this.notifyCataclysm = notifyCataclysm;
        this.time_notify = time_notify;
    }

    public User() {
        this.notifyPrecipitation = false;
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

    public Time getTimeNotify() {
        return time_notify;
    }

    public void setTimeNotify(Time time_notify) {
        this.time_notify = time_notify;
    }

    @Override
    public String toString() {
        return "User{" +
                "telegram_id=" + telegram_id +
                ", cityName='" + cityName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", scheduleTime=" + scheduleTime +
                ", notifyPrecipitation=" + notifyPrecipitation +
                ", notifyCataclysm=" + notifyCataclysm +
                '}';
    }
}
