package com.github.zeykrus.headachetracker.bot;

import java.time.LocalDateTime;

public class User {
    private UserState state;
    private LocalDateTime dateTime;
    private Integer intensity;
    private String location;
    private String symptoms;
    private String triggers;
    private String comment;
    
    public User() {
        state = UserState.IDLE;
        dateTime = null;
        intensity = null;
        location = null;
        symptoms = null;
        triggers = null;
        comment = null;
    }
    
    public void nextAddState() {
        switch (state) {
            case EXCEPTION_WITH_SAVE -> {
                return;
            }
            case IDLE -> state = UserState.AWAITING_DATE_TIME;
            case AWAITING_DATE_TIME -> state = UserState.AWAITING_INTENSITY;
            case AWAITING_INTENSITY -> state = UserState.AWAITING_LOCATION;
            case AWAITING_LOCATION -> state = UserState.AWAITING_SYMPTOMS;
            case AWAITING_SYMPTOMS -> state = UserState.AWAITING_TRIGGERS;
            case AWAITING_TRIGGERS -> state = UserState.AWAITING_COMMENT;
            default -> state = UserState.IDLE;
        }
    }
    
    public void reset() {
        dateTime = null;
        intensity = null;
        location = null;
        symptoms = null;
        triggers = null;
        comment = null;
    }
    
    public UserState getState() {
        return state;
    }
    
    public void setState(UserState state) {
        this.state = state;
    }
    
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
    
    public Integer getIntensity() {
        return intensity;
    }
    
    public void setIntensity(Integer intensity) {
        this.intensity = intensity;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    public String getTriggers() {
        return triggers;
    }
    
    public void setTriggers(String triggers) {
        this.triggers = triggers;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
}
