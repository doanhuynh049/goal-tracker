import org.junit.Test;
import org.junit.Assert;
import model.Goal;
import model.GoalType;
import model.Task;
import service.GoalService;
import java.time.LocalDate;

public class HierarchicalGoalTest {
    
    @Test
    public void testHierarchicalGoalStructure() {
        GoalService service = new GoalService();
        
        // Create the example hierarchical structure
        Goal longTermGoal = service.createExampleHierarchy();
        
        // Verify structure
        Assert.assertNotNull("Long-term goal should be created", longTermGoal);
        Assert.assertEquals("Should be LONG_TERM type", GoalType.LONG_TERM, longTermGoal.getType());
        Assert.assertEquals("Should have correct title", "Become Senior Developer", longTermGoal.getTitle());
        
        // Check yearly goal
        Assert.assertEquals("Should have 1 sub-goal", 1, longTermGoal.getSubGoals().size());
        Goal yearlyGoal = longTermGoal.getSubGoals().get(0);
        Assert.assertEquals("Should be YEARLY type", GoalType.YEARLY, yearlyGoal.getType());
        Assert.assertEquals("Should have correct title", "2025 - Master Java & AI", yearlyGoal.getTitle());
        
        // Check monthly goal
        Assert.assertEquals("Should have 1 sub-goal", 1, yearlyGoal.getSubGoals().size());
        Goal monthlyGoal = yearlyGoal.getSubGoals().get(0);
        Assert.assertEquals("Should be MONTHLY type", GoalType.MONTHLY, monthlyGoal.getType());
        Assert.assertEquals("Should have correct title", "August - Learn Lambda Expressions", monthlyGoal.getTitle());
        
        // Check tasks
        Assert.assertEquals("Should have 2 tasks", 2, monthlyGoal.getTasks().size());
        
        // Test hierarchical path
        String expectedPath = "Become Senior Developer > 2025 - Master Java & AI > August - Learn Lambda Expressions";
        Assert.assertEquals("Should have correct hierarchical path", expectedPath, monthlyGoal.getHierarchicalPath());
        
        // Test hierarchy levels
        Assert.assertEquals("Long-term goal should be level 0", 0, longTermGoal.getHierarchyLevel());
        Assert.assertEquals("Yearly goal should be level 1", 1, yearlyGoal.getHierarchyLevel());
        Assert.assertEquals("Monthly goal should be level 2", 2, monthlyGoal.getHierarchyLevel());
        
        // Test progress calculation (should include all tasks from sub-goals)
        Assert.assertEquals("All tasks should be included in progress", 2, longTermGoal.getAllTasks().size());
    }
    
    @Test
    public void testCanHaveChild() {
        Goal longTermGoal = new Goal("Test Long Term", GoalType.LONG_TERM, LocalDate.now());
        Goal yearlyGoal = new Goal("Test Yearly", GoalType.YEARLY, LocalDate.now());
        
        // Long-term can have yearly as child
        Assert.assertTrue("Long-term should accept yearly child", longTermGoal.canHaveChild(GoalType.YEARLY));
        Assert.assertTrue("Long-term should accept monthly child", longTermGoal.canHaveChild(GoalType.MONTHLY));
        Assert.assertFalse("Long-term should not accept another long-term child", longTermGoal.canHaveChild(GoalType.LONG_TERM));
        
        // Yearly can have monthly as child
        Assert.assertTrue("Yearly should accept monthly child", yearlyGoal.canHaveChild(GoalType.MONTHLY));
        Assert.assertTrue("Yearly should accept weekly child", yearlyGoal.canHaveChild(GoalType.WEEKLY));
        Assert.assertFalse("Yearly should not accept yearly child", yearlyGoal.canHaveChild(GoalType.YEARLY));
    }
    
    @Test
    public void testRootGoals() {
        GoalService service = new GoalService();
        
        // Create some test goals
        Goal root1 = service.createHierarchicalGoal("Root 1", GoalType.LONG_TERM, LocalDate.now(), null);
        Goal root2 = service.createHierarchicalGoal("Root 2", GoalType.YEARLY, LocalDate.now(), null);
        Goal child1 = service.createHierarchicalGoal("Child 1", GoalType.MONTHLY, LocalDate.now(), root1);
        
        // Test root goals
        Assert.assertEquals("Should have 2 root goals", 2, service.getRootGoals().size());
        Assert.assertTrue("Should contain root1", service.getRootGoals().contains(root1));
        Assert.assertTrue("Should contain root2", service.getRootGoals().contains(root2));
        Assert.assertFalse("Should not contain child1", service.getRootGoals().contains(child1));
        
        // Test flattened goals
        Assert.assertEquals("Should have 3 goals total", 3, service.getAllGoalsFlattened().size());
    }
}
