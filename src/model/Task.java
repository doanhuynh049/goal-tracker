package model;

import java.time.LocalDate;
import java.util.UUID;

public class Task {
    private UUID id;
    private String title;
    private String description;
    private Priority priority;
    private boolean completed;
    private LocalDate dueDate;
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    // New constructor
    public Task(String title, String description, LocalDate dueDate) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = false;
        this.priority = Priority.MEDIUM;
    }

    public String getTitle() {
        return title;
    }

    public void markAsCompleted() {
        this.completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }
    public UUID getId() {
        return id;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
}
