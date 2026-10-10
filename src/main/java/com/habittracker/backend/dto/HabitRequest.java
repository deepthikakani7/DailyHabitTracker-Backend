package com.habittracker.backend.dto;

public class HabitRequest {

    private String name;
    private String description;

    public HabitRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}