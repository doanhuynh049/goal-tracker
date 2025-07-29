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
                    LocalDate start = task.getStartDate();
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
}
