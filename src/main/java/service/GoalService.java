package service;

import model.Goal;
import model.GoalType;
import model.Task;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GoalService {
    private List<Goal> goals = new ArrayList<>();

    public void createGoal(String title, GoalType type, LocalDate targetDate) {
        Goal goal = new Goal(title, "", type, targetDate, null);
        goals.add(goal);
    }

    public void addTaskToGoal(UUID goalId, Task task) {
        for (Goal goal : goals) {
            if (goal.getId().equals(goalId)) {
                goal.addTask(task);
                break;
            }
        }
    }

    public void markTaskCompleted(UUID taskId) {
        for (Goal goal : goals) {
            for (Task task : goal.getTasks()) {
                if (task.getId().equals(taskId)) {
                    task.markAsCompleted();
                    return;
                }
            }
        }
    }

    public void markTaskCompleted(String taskIdStr) {
        UUID taskId = UUID.fromString(taskIdStr);
        markTaskCompleted(taskId);
    }

    public List<Task> filterTasks(UUID goalId, Predicate<Task> filter) {
        for (Goal goal : goals) {
            if (goal.getId().equals(goalId)) {
                return goal.getTasks().stream().filter(filter).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    public List<Task> sortTasks(UUID goalId, Comparator<Task> comparator) {
        for (Goal goal : goals) {
            if (goal.getId().equals(goalId)) {
                List<Task> sorted = new ArrayList<>(goal.getTasks());
                sorted.sort(comparator);
                return sorted;
            }
        }
        return Collections.emptyList();
    }

    public List<Goal> getAllGoals() {
        return goals;
    }

    // Find a goal by its title
    public Goal getGoalByTitle(String title) {
        for (Goal goal : goals) {
            if (goal.getTitle().equalsIgnoreCase(title)) {
                return goal;
            }
        }
        return null;
    }

    // Save all goals to Excel file
    public void saveGoalsToFile() {
        try {
            util.FileUtil.saveGoalsAndTasks(goals);
            System.out.println("Goals saved to Excel file successfully");
        } catch (Exception e) {
            System.err.println("Error saving goals: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load all goals from Excel file
    public void loadGoalsFromFile() {
        try {
            List<Goal> loaded = util.FileUtil.loadGoalsAndTasks();
            goals.clear();
            goals.addAll(loaded);
            System.out.println("Goals loaded from Excel file successfully");
        } catch (Exception e) {
            System.err.println("Error loading goals: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addGoal(Goal goal) {
        goals.add(goal);
    }

    public void deleteGoal(Goal goal) {
        goals.remove(goal);
    }

    public List<Task> getTodayTasks() {
        LocalDate today = LocalDate.now();
        return getAllTasks().stream()
                .filter(task -> {
                    LocalDate start = task.getStartDay();
                    LocalDate due = task.getDueDate();
                    boolean inProgress = (start != null && !start.isAfter(today)) && (due != null && !due.isBefore(today));
                    boolean notCompleted = !task.isCompleted();
                    return inProgress && notCompleted;
                })
                .collect(Collectors.toList());
    }

    private List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        for (Goal goal : goals) {
            allTasks.addAll(goal.getTasks());
        }
        return allTasks;
    }

    // Get number of completed tasks across all goals
    public int getCompletedTaskCount() {
        return (int) getAllTasks().stream().filter(Task::isCompleted).count();
    }

    // Get number of pending (not completed) tasks across all goals
    public int getPendingTaskCount() {
        return (int) getAllTasks().stream().filter(t -> !t.isCompleted()).count();
    }

    // Get all goals
    public List<Goal> getGoals() {
        return getAllGoals();
    }
    
    // Hierarchical Goal Service Methods
    
    public Goal createLongTermGoal(String title, String description, LocalDate targetDate) {
        Goal longTermGoal = new Goal(title, description, GoalType.LONG_TERM_GOAL, targetDate, null);
        goals.add(longTermGoal);
        return longTermGoal;
    }
    
    public Goal createYearGoal(String title, String description, LocalDate targetDate, Goal parentLongTermGoal) {
        Goal yearGoal = new Goal(title, description, GoalType.YEAR_GOAL, targetDate, null);
        
        if (parentLongTermGoal != null && parentLongTermGoal.getType() == GoalType.LONG_TERM_GOAL) {
            parentLongTermGoal.addChildGoal(yearGoal);
        } else {
            goals.add(yearGoal); // Add as standalone if no valid parent
        }
        
        return yearGoal;
    }
    
    public Goal createMonthGoal(String title, String description, LocalDate targetDate, Goal parentYearGoal) {
        Goal monthGoal = new Goal(title, description, GoalType.MONTH_GOAL, targetDate, null);
        
        if (parentYearGoal != null && parentYearGoal.getType() == GoalType.YEAR_GOAL) {
            parentYearGoal.addChildGoal(monthGoal);
        } else {
            goals.add(monthGoal); // Add as standalone if no valid parent
        }
        
        return monthGoal;
    }
    
    // Add a child goal to a parent goal
    public void addChildGoal(UUID parentGoalId, Goal childGoal) {
        Goal parentGoal = findGoalById(parentGoalId);
        if (parentGoal != null) {
            parentGoal.addChildGoal(childGoal);
        }
    }
    
    // Find a goal by ID (including child goals)
    public Goal findGoalById(UUID goalId) {
        for (Goal goal : goals) {
            if (goal.getId().equals(goalId)) {
                return goal;
            }
            Goal found = findGoalInHierarchy(goal, goalId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    // Recursive helper to find goal in hierarchy
    private Goal findGoalInHierarchy(Goal goal, UUID goalId) {
        for (Goal child : goal.getChildGoals()) {
            if (child.getId().equals(goalId)) {
                return child;
            }
            Goal found = findGoalInHierarchy(child, goalId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    // Get all root goals (goals without parents)
    public List<Goal> getRootGoals() {
        return goals.stream()
                .filter(Goal::isRootGoal)
                .collect(Collectors.toList());
    }
    
    // Get all long-term goals
    public List<Goal> getLongTermGoals() {
        return goals.stream()
                .filter(goal -> goal.getType() == GoalType.LONG_TERM_GOAL)
                .collect(Collectors.toList());
    }
    
    // Get all year goals for a specific long-term goal
    public List<Goal> getYearGoalsForLongTerm(Goal longTermGoal) {
        return longTermGoal.getChildGoals().stream()
                .filter(goal -> goal.getType() == GoalType.YEAR_GOAL)
                .collect(Collectors.toList());
    }
    
    // Get all month goals for a specific year goal
    public List<Goal> getMonthGoalsForYear(Goal yearGoal) {
        return yearGoal.getChildGoals().stream()
                .filter(goal -> goal.getType() == GoalType.MONTH_GOAL)
                .collect(Collectors.toList());
    }
    
    // Get hierarchical structure as a tree
    public void printGoalHierarchy() {
        for (Goal rootGoal : getRootGoals()) {
            printGoalTree(rootGoal, 0);
        }
    }
    
    private void printGoalTree(Goal goal, int level) {
        StringBuilder indentBuilder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indentBuilder.append("  ");
        }
        String indent = indentBuilder.toString();
        System.out.println(indent + "- " + goal.getTitle() + " (" + goal.getType() + ")");
        
        for (Goal child : goal.getChildGoals()) {
            printGoalTree(child, level + 1);
        }
        
        if (!goal.getTasks().isEmpty()) {
            for (Task task : goal.getTasks()) {
                System.out.println(indent + "  * " + task.getTitle());
            }
        }
    }
    
    // Create example hierarchical structure
    public void createExampleHierarchy() {
        // Create Long-term goal
        Goal longTermGoal = createLongTermGoal(
            "Become Senior Developer", 
            "Master advanced programming skills and leadership", 
            LocalDate.of(2027, 12, 31)
        );
        
        // Create Year goal
        Goal yearGoal2025 = createYearGoal(
            "2025 - Master Java & AI", 
            "Focus on Java advanced features and AI integration", 
            LocalDate.of(2025, 12, 31), 
            longTermGoal
        );
        
        // Create Month goal
        Goal monthGoalJuly = createMonthGoal(
            "July - Learn Lambda Expressions", 
            "Deep dive into Java functional programming", 
            LocalDate.of(2025, 7, 31), 
            yearGoal2025
        );
        
        // Add tasks to the month goal
        Task task1 = new Task("Complete Udemy course on Java Lambdas", 
                             "Finish the comprehensive Lambda expressions course", 
                             LocalDate.of(2025, 7, 15));
        Task task2 = new Task("Build a small Java project using functional interfaces", 
                             "Create a practical application demonstrating Lambda usage", 
                             LocalDate.of(2025, 7, 30));
        
        monthGoalJuly.addTask(task1);
        monthGoalJuly.addTask(task2);
    }
}
