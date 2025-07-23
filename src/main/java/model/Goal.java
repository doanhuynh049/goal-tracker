package model;

import java.time.LocalDate;
import java.util.UUID;
import java.util.*;

public class Goal {
    private UUID id;
    private String title;
    private String description;
    private GoalType type;
    private boolean completed;
    private LocalDate targetDate;
    private List<Task> tasks;

    public Goal(String title, String description, GoalType type, LocalDate targetDate, String id) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.type = type;
        this.targetDate = targetDate;
        this.tasks = new ArrayList<>();
    }

    public Goal(String title, GoalType type, LocalDate targetDate) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = "";
        this.type = type;
        this.targetDate = targetDate;
        this.tasks = new ArrayList<>();
    }

    // Add a task to this goal
    public void addTask(Task task) {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        tasks.add(task);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }

    public double getProgress() {
        if (tasks.isEmpty()) {
            return 0.0;
        }

        long completedTasks = tasks.stream().filter(Task::isCompleted).count();
        return (double) completedTasks / tasks.size() * 100;
    }

    public String getTitle() {
        return title;
    }

    // Get all tasks for this goal
    public List<Task> getTasks() {
        return tasks;
    }

    public UUID getId() {
        return id;
    }

    public GoalType getType() {
        return type;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }
}
