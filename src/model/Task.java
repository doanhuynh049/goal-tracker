package model;

import java.time.LocalDate;
import java.util.UUID;

public class Task {
    private UUID id;
    private String name;
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

    public boolean isCompleted() {
        return completed;
    }
}
