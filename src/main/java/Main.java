import model.Goal;
import model.GoalType;
import model.Task;
import util.FileUtil;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // This is the main testing function
        try {
            // Create sample goals and tasks
            Goal myGoal = new Goal("Learn Java", "Complete the Java course", GoalType.LONG_TERM, LocalDate.of(2024, 12, 31), null);
            Task task1 = new Task("Complete Chapter 1", "Read and understand the first chapter", LocalDate.of(2024, 6, 30));
            Task task2 = new Task("Complete Chapter 2", "Read and understand the second chapter", LocalDate.of(2024, 7, 15));
            myGoal.addTask(task1);
            myGoal.addTask(task2);
            task1.markAsCompleted();
            List<Goal> goalsToSave = Arrays.asList(myGoal);
            // Save to Excel
            FileUtil.saveGoalsAndTasks(goalsToSave);
            System.out.println("Goals and tasks saved to Excel.");
            // Load from Excel
            List<Goal> loadedGoals = FileUtil.loadGoalsAndTasks();
            System.out.println("Loaded goals and tasks from Excel:");
            for (Goal g : loadedGoals) {
                System.out.println("Goal: " + g.getTitle() + " (" + g.getType() + ", Target: " + g.getTargetDate() + ")");
                for (Task t : g.getTasks()) {
                    System.out.println("  - Task: " + t.getTitle() + ", Due: " + t.getDueDate() + ", Completed: " + t.isCompleted() + ", Priority: " + t.getPriority() + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
