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
    private java.time.LocalDate completedDate;
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

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    // New constructor for Excel loading
    public Task(UUID id, String description, LocalDate dueDate, Priority priority, boolean completed) {
        this.id = id;
        this.title = "";
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
    }

    // New constructor for full task info
    public Task(String title, String description, LocalDate dueDate, Priority priority, boolean completed) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
    }

    // Add setters for editable fields
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            this.completedDate = java.time.LocalDate.now();
        } else {
            this.completedDate = null;
        }
    }
    public java.time.LocalDate getCompletedDate() {
        return completedDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
