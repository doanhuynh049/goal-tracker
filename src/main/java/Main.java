import model.Goal;
import model.GoalType;
import model.Task;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        // This is the main testing function
        Goal myGoal = new Goal("Learn Java", "Complete the Java course", GoalType.LONG_TERM, LocalDate.of(2024, 12, 31), null);
        Task task1 = new Task("Complete Chapter 1", "Read and understand the first chapter", LocalDate.of(2024, 6, 30));
        Task task2 = new Task("Complete Chapter 2", "Read and understand the second chapter", LocalDate.of(2024, 7, 15));
        myGoal.addTask(task1);
        myGoal.addTask(task2);
        task1.markAsCompleted(); // Simulate completing the first task
        System.out.println("Goal Title: " + myGoal.getTitle());
        System.out.println("Goal Progress: " + myGoal.getProgress() + "%");
        System.out.println("Tasks:");
        for (Task task : myGoal.getTasks()) {
            System.out.println("- " + task.getTitle() + " (Completed: " + task.isCompleted() + ")");
        }
    }
}
