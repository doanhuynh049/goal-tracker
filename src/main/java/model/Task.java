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
    private LocalDate startDay;
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    public enum TaskStatus {
        TO_DO("To Do"),
        IN_PROGRESS("In Progress"),
        DONE("Done");

        private final String displayName;
        TaskStatus(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
        public static TaskStatus fromString(String value) {
            for (TaskStatus s : values()) {
                if (s.displayName.equalsIgnoreCase(value) || s.name().replace('_',' ').equalsIgnoreCase(value)) {
                    return s;
                }
            }
            return TO_DO; // default
        }
    }

    private TaskStatus status = TaskStatus.TO_DO;

    // New constructor
    public Task(String title, String description, LocalDate dueDate) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = false;
        this.priority = Priority.MEDIUM;
        this.status = TaskStatus.TO_DO;
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

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDate getStartDay() {
        return startDay;
    }

    // New constructor for Excel loading
    public Task(UUID id, String description, LocalDate dueDate, Priority priority, boolean completed, TaskStatus status) {
        this.id = id;
        this.title = "";
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
        this.status = status != null ? status : TaskStatus.TO_DO;
    }

    // New constructor for full task info
    public Task(String title, String description, LocalDate dueDate, Priority priority, boolean completed, TaskStatus status) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
        this.status = status != null ? status : TaskStatus.TO_DO;
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
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setStartDay(LocalDate startDay) {
        this.startDay = startDay;
    }

    // Update constructors to include startDay
    public Task(String title, String description, Priority priority, LocalDate startDay, LocalDate dueDate, TaskStatus status, boolean completed) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.startDay = startDay;
        this.dueDate = dueDate;
        this.status = status;
        this.completed = completed;
    }

    // Add overloaded constructors for backward compatibility
    public Task(String title, String description, Priority priority, LocalDate dueDate, TaskStatus status, boolean completed) {
        this(title, description, priority, null, dueDate, status, completed);
    }
    public Task(String title, String description, Priority priority, LocalDate dueDate) {
        this(title, description, priority, null, dueDate, TaskStatus.TO_DO, false);
    }

    public Task(UUID id, String title, String description, Priority priority, LocalDate startDay, LocalDate dueDate, TaskStatus status, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.startDay = startDay;
        this.dueDate = dueDate;
        this.status = status != null ? status : TaskStatus.TO_DO;
        this.completed = completed;
    }
}
