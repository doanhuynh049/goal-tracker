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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load all goals from Excel file
    public void loadGoalsFromFile() {
        try {
            List<Goal> loaded = util.FileUtil.loadGoalsAndTasks();
            goals.clear();
            goals.addAll(loaded);
        } catch (Exception e) {
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
        for (Goal goal : getAllGoalsFlattened()) {
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
    
    // Hierarchical goal methods
    public Goal createHierarchicalGoal(String title, GoalType type, LocalDate targetDate, Goal parentGoal) {
        Goal goal = new Goal(title, type, targetDate, parentGoal);
        
        // Only add to root goals list if it has no parent
        if (parentGoal == null) {
            goals.add(goal);
        }
        
        return goal;
    }
    
    public List<Goal> getRootGoals() {
        return goals.stream()
                .filter(goal -> goal.getParentGoal() == null)
                .collect(Collectors.toList());
    }
    
    public List<Goal> getAllGoalsFlattened() {
        List<Goal> allGoals = new ArrayList<>();
        for (Goal rootGoal : getRootGoals()) {
            addGoalAndDescendants(rootGoal, allGoals);
        }
        return allGoals;
    }
    
    private void addGoalAndDescendants(Goal goal, List<Goal> allGoals) {
        allGoals.add(goal);
        for (Goal subGoal : goal.getSubGoals()) {
            addGoalAndDescendants(subGoal, allGoals);
        }
    }
    
    public List<Goal> getGoalsByType(GoalType type) {
        return getAllGoalsFlattened().stream()
                .filter(goal -> goal.getType() == type)
                .collect(Collectors.toList());
    }
    
    public List<Goal> getGoalsByHierarchyLevel(int level) {
        return getAllGoalsFlattened().stream()
                .filter(goal -> goal.getHierarchyLevel() == level)
                .collect(Collectors.toList());
    }
    
    public Goal findGoalById(UUID goalId) {
        return getAllGoalsFlattened().stream()
                .filter(goal -> goal.getId().equals(goalId))
                .findFirst()
                .orElse(null);
    }
    
    public void deleteGoalHierarchically(Goal goalToDelete) {
        // Remove from parent's sub-goals if it has a parent
        if (goalToDelete.getParentGoal() != null) {
            goalToDelete.getParentGoal().removeSubGoal(goalToDelete);
        } else {
            // Remove from root goals list
            goals.remove(goalToDelete);
        }
    }
    
    // Get example hierarchical structure
    public Goal createExampleHierarchy() {
        // Create long-term goal
        Goal longTermGoal = createHierarchicalGoal(
            "Become Senior Developer", 
            GoalType.LONG_TERM, 
            LocalDate.now().plusYears(3), 
            null
        );
        
        // Create yearly goal
        Goal yearlyGoal = createHierarchicalGoal(
            "2025 - Master Java & AI", 
            GoalType.YEARLY, 
            LocalDate.of(2025, 12, 31), 
            longTermGoal
        );
        
        // Create monthly goal
        Goal monthlyGoal = createHierarchicalGoal(
            "August - Learn Lambda Expressions", 
            GoalType.MONTHLY, 
            LocalDate.of(2025, 8, 31), 
            yearlyGoal
        );
        
        // Add tasks to monthly goal
        Task task1 = new Task("Complete Udemy course on Java Lambdas", 
                             "Study functional programming concepts and lambda expressions in Java", 
                             LocalDate.of(2025, 8, 15));
        
        Task task2 = new Task("Build a small Java project using functional interfaces", 
                             "Create a practical application that demonstrates lambda usage", 
                             LocalDate.of(2025, 8, 30));
        
        monthlyGoal.addTask(task1);
        monthlyGoal.addTask(task2);
        
        return longTermGoal;
    }
}
