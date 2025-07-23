// This class implement unites tests for the Goal class.
// It uses the JUnit framework for testing.
package test;
import static org.junit.Assert.assertEquals;
import model.Goal;
import model.GoalType;
import model.Task;
import java.time.LocalDate;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

public class GoalTest {
    private Goal goal;

    @Before
    public void setUp() {
        goal = new Goal("Test Goal", "This is a test goal.", GoalType.LONG_TERM, LocalDate.of(2025, 12, 31), null);
    }

    @After
    
    public void tearDown() {
        goal = null;
    }

    @Test
    public void testAddTaskAndProgress() {
        Task task1 = new Task();
        Task task2 = new Task();
        goal.addTask(task1);
        goal.addTask(task2);
        assertEquals(0.0, goal.getProgress(), 0.01);
    }

    @Test
    public void testRemoveTask() {
        Task task = new Task();
        goal.addTask(task);
        goal.removeTask(task);
        assertEquals(0.0, goal.getProgress(), 0.01);
    }

    @Test
    public void testProgressWithCompletedTasks() {
        Task task1 = new Task() {
            @Override
            public boolean isCompleted() { return true; }
        };
        Task task2 = new Task() {
            @Override
            public boolean isCompleted() { return false; }
        };
        goal.addTask(task1);
        goal.addTask(task2);
        assertEquals(50.0, goal.getProgress(), 0.01);
    }
}
