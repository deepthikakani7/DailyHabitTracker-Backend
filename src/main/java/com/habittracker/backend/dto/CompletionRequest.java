package com.habittracker.backend.dto;

import java.time.LocalDate;

public class CompletionRequest {

    private LocalDate date;
    private boolean completed;

    public CompletionRequest() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}