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
}
