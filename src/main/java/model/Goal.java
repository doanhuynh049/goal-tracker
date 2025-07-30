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
    
    // Hierarchical relationships
    private Goal parentGoal;
    private List<Goal> childGoals;
    private int hierarchyLevel; // 0=LongTerm, 1=Year, 2=Month, 3=Task level

    public Goal(String title, String description, GoalType type, LocalDate targetDate, String id) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.type = type;
        this.targetDate = targetDate;
        this.tasks = new ArrayList<>();
        this.childGoals = new ArrayList<>();
        this.hierarchyLevel = determineHierarchyLevel(type);
    }

    public Goal(String title, GoalType type, LocalDate targetDate) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = "";
        this.type = type;
        this.targetDate = targetDate;
        this.tasks = new ArrayList<>();
        this.childGoals = new ArrayList<>();
        this.hierarchyLevel = determineHierarchyLevel(type);
    }
    
    // Helper method to determine hierarchy level based on goal type
    private int determineHierarchyLevel(GoalType type) {
        switch (type) {
            case LONG_TERM_GOAL:
                return 0;
            case YEAR_GOAL:
                return 1;
            case MONTH_GOAL:
                return 2;
            default:
                return 3; // Task level or other types
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
    
    // Hierarchical Goal Methods
    
    public void addChildGoal(Goal childGoal) {
        if (childGoals == null) {
            childGoals = new ArrayList<>();
        }
        childGoals.add(childGoal);
        childGoal.setParentGoal(this);
    }
    
    public void removeChildGoal(Goal childGoal) {
        if (childGoals != null) {
            childGoals.remove(childGoal);
            childGoal.setParentGoal(null);
        }
    }
    
    public List<Goal> getChildGoals() {
        return childGoals != null ? new ArrayList<>(childGoals) : new ArrayList<>();
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
    
    public boolean hasChildren() {
        return childGoals != null && !childGoals.isEmpty();
    }
    
    public boolean isRootGoal() {
        return parentGoal == null;
    }
    
    // Get all descendant goals (recursive)
    public List<Goal> getAllDescendants() {
        List<Goal> descendants = new ArrayList<>();
        if (childGoals != null) {
            for (Goal child : childGoals) {
                descendants.add(child);
                descendants.addAll(child.getAllDescendants());
            }
        }
        return descendants;
    }
    
    // Get hierarchical progress including child goals
    public double getHierarchicalProgress() {
        double totalProgress = 0;
        int totalItems = 0;
        
        // Include own task progress
        if (!tasks.isEmpty()) {
            totalProgress += getProgress();
            totalItems++;
        }
        
        // Include child goal progress
        if (childGoals != null && !childGoals.isEmpty()) {
            for (Goal child : childGoals) {
                totalProgress += child.getHierarchicalProgress();
                totalItems++;
            }
        }
        
        return totalItems > 0 ? totalProgress / totalItems : 0.0;
    }
    
    // Get the full hierarchy path (e.g., "Long Term > 2025 > July")
    public String getHierarchyPath() {
        if (parentGoal == null) {
            return title;
        }
        return parentGoal.getHierarchyPath() + " > " + title;
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
}
