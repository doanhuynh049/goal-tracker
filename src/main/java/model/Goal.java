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
    private String notes = "";
    private java.time.LocalDateTime lastUpdated;
    
    // Hierarchical fields
    private Goal parentGoal;
    private List<Goal> subGoals;
    private int hierarchyLevel; // 0 = root (long-term), 1 = yearly, 2 = monthly, 3 = task level

    public Goal(String title, String description, GoalType type, LocalDate targetDate, String id) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.type = type;
        this.targetDate = targetDate;
        this.tasks = new ArrayList<>();
        this.subGoals = new ArrayList<>();
        this.hierarchyLevel = determineHierarchyLevel(type);
    }

    public Goal(String title, GoalType type, LocalDate targetDate) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = "";
        this.type = type;
        this.targetDate = targetDate;
        this.tasks = new ArrayList<>();
        this.subGoals = new ArrayList<>();
        this.hierarchyLevel = determineHierarchyLevel(type);
    }
    
    // Constructor for hierarchical goals
    public Goal(String title, GoalType type, LocalDate targetDate, Goal parentGoal) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = "";
        this.type = type;
        this.targetDate = targetDate;
        this.tasks = new ArrayList<>();
        this.subGoals = new ArrayList<>();
        this.parentGoal = parentGoal;
        this.hierarchyLevel = determineHierarchyLevel(type);
        
        if (parentGoal != null) {
            parentGoal.addSubGoal(this);
        }
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
        // Use hierarchical progress calculation that includes sub-goals
        return getHierarchicalProgress();
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

    public void setType(GoalType type) {
        this.type = type;
    }
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
        updateLastModified();
    }
    public void setTitle(String title) {
        this.title = title;
        updateLastModified();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        updateLastModified();
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        updateLastModified();
    }
    
    public java.time.LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    public void updateLastModified() {
        this.lastUpdated = java.time.LocalDateTime.now();
    }
    // Optionally: getProgressPercent for UI compatibility
    public double getProgressPercent() {
        return getProgress() / 100.0;
    }

    // Get goal name (for chart display)
    public String getName() {
        return getTitle();
    }

    // Get completion percentage (0-100)
    public double getCompletionPercentage() {
        return getProgress();
    }
    
    // Hierarchical methods
    public void addSubGoal(Goal subGoal) {
        if (subGoals == null) {
            subGoals = new ArrayList<>();
        }
        if (!subGoals.contains(subGoal)) {
            subGoals.add(subGoal);
            subGoal.setParentGoal(this);
        }
    }
    
    public void removeSubGoal(Goal subGoal) {
        if (subGoals != null) {
            subGoals.remove(subGoal);
            subGoal.setParentGoal(null);
        }
    }
    
    public List<Goal> getSubGoals() {
        return subGoals != null ? new ArrayList<>(subGoals) : new ArrayList<>();
    }
    
    public Goal getParentGoal() {
        return parentGoal;
    }
    
    public void setParentGoal(Goal parentGoal) {
        this.parentGoal = parentGoal;
    }
    
    public int getHierarchyLevel() {
        return hierarchyLevel;
    }
    
    public void setHierarchyLevel(int level) {
        this.hierarchyLevel = level;
    }
    
    // Determine hierarchy level based on goal type
    private int determineHierarchyLevel(GoalType type) {
        switch (type) {
            case LONG_TERM:
                return 0; // Root level
            case YEARLY:
                return 1; // Year level
            case MONTHLY:
                return 2; // Month level
            case WEEKLY:
                return 3; // Week level
            case SHORT_TERM:
                return 2; // Similar to monthly
            case DAILY:
                return 4; // Day level
            default:
                return 2; // Default to month level
        }
    }
    
    // Get all tasks including from sub-goals (recursive)
    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>(tasks);
        if (subGoals != null) {
            for (Goal subGoal : subGoals) {
                allTasks.addAll(subGoal.getAllTasks());
            }
        }
        return allTasks;
    }
    
    // Calculate progress including sub-goals
    public double getHierarchicalProgress() {
        List<Task> allTasks = getAllTasks();
        if (allTasks.isEmpty()) {
            return 0.0;
        }
        
        long completedTasks = allTasks.stream().filter(Task::isCompleted).count();
        return (double) completedTasks / allTasks.size() * 100;
    }
    
    // Check if this goal can have the specified goal type as a child
    public boolean canHaveChild(GoalType childType) {
        int childLevel = determineHierarchyLevel(childType);
        return childLevel > this.hierarchyLevel;
    }
    
    // Get the root goal (top-most parent)
    public Goal getRootGoal() {
        Goal root = this;
        while (root.getParentGoal() != null) {
            root = root.getParentGoal();
        }
        return root;
    }
    
    // Get the full hierarchical path as string
    public String getHierarchicalPath() {
        if (parentGoal == null) {
            return title;
        }
        return parentGoal.getHierarchicalPath() + " > " + title;
    }
}
