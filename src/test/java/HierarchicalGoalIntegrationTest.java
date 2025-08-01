import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import model.Goal;
import model.GoalType;
import model.Task;
import service.GoalService;
import java.time.LocalDate;
import java.util.List;

/**
 * Integration test that demonstrates the complete hierarchical goal system
 * including real-world scenarios and advanced features.
 */
public class HierarchicalGoalIntegrationTest {
    
    private GoalService service;
    
    @Before
    public void setUp() {
        service = new GoalService();
    }
    
    @Test
    public void testCompleteCareerDevelopmentHierarchy() {
        System.out.println("=== Testing Career Development Hierarchy ===");
        
        // Create long-term career goal
        Goal careerGoal = service.createHierarchicalGoal(
            "Become Senior Software Architect", 
            GoalType.LONG_TERM, 
            LocalDate.of(2027, 12, 31), 
            null
        );
        
        // Create yearly goals
        Goal year2025 = service.createHierarchicalGoal(
            "2025 - Master Advanced Java & System Design", 
            GoalType.YEARLY, 
            LocalDate.of(2025, 12, 31), 
            careerGoal
        );
        
        Goal year2026 = service.createHierarchicalGoal(
            "2026 - Leadership & Architecture Skills", 
            GoalType.YEARLY, 
            LocalDate.of(2026, 12, 31), 
            careerGoal
        );
        
        // Create monthly goals for 2025
        Goal aug2025 = service.createHierarchicalGoal(
            "August 2025 - Microservices Architecture", 
            GoalType.MONTHLY, 
            LocalDate.of(2025, 8, 31), 
            year2025
        );
        
        Goal sep2025 = service.createHierarchicalGoal(
            "September 2025 - Spring Boot Advanced", 
            GoalType.MONTHLY, 
            LocalDate.of(2025, 9, 30), 
            year2025
        );
        
        // Add tasks to August 2025
        Task task1 = new Task("Complete Microservices Design Patterns Course", 
                             "Study microservices patterns, API gateway, service mesh", 
                             LocalDate.of(2025, 8, 15));
        Task task2 = new Task("Build Sample Microservices Application", 
                             "Create a multi-service application with Docker and Kubernetes", 
                             LocalDate.of(2025, 8, 30));
        
        aug2025.addTask(task1);
        aug2025.addTask(task2);
        
        // Add tasks to September 2025
        Task task3 = new Task("Advanced Spring Security Implementation", 
                             "OAuth2, JWT, and security best practices", 
                             LocalDate.of(2025, 9, 15));
        Task task4 = new Task("Performance Optimization Project", 
                             "Database tuning, caching strategies, monitoring", 
                             LocalDate.of(2025, 9, 30));
        
        sep2025.addTask(task3);
        sep2025.addTask(task4);
        
        // Test hierarchy structure
        Assert.assertEquals("Career goal should have 2 yearly sub-goals", 2, careerGoal.getSubGoals().size());
        Assert.assertEquals("2025 goal should have 2 monthly sub-goals", 2, year2025.getSubGoals().size());
        Assert.assertEquals("August goal should have 2 tasks", 2, aug2025.getTasks().size());
        
        // Test hierarchical paths
        String expectedAugPath = "Become Senior Software Architect > 2025 - Master Advanced Java & System Design > August 2025 - Microservices Architecture";
        Assert.assertEquals("August goal path should be correct", expectedAugPath, aug2025.getHierarchicalPath());
        
        // Test all tasks aggregation
        List<Task> allTasks = careerGoal.getAllTasks();
        Assert.assertEquals("Career goal should aggregate all 4 tasks", 4, allTasks.size());
        
        // Test hierarchy levels
        Assert.assertEquals("Career goal should be level 0", 0, careerGoal.getHierarchyLevel());
        Assert.assertEquals("Yearly goals should be level 1", 1, year2025.getHierarchyLevel());
        Assert.assertEquals("Monthly goals should be level 2", 2, aug2025.getHierarchyLevel());
        
        System.out.println("✅ Career development hierarchy test passed!");
        System.out.println("   📊 Total goals: " + service.getAllGoalsFlattened().size());
        System.out.println("   📋 Total tasks: " + careerGoal.getAllTasks().size());
        System.out.println("   🎯 Hierarchy depth: 3 levels");
    }
    
    @Test
    public void testProgressCalculationAcrossHierarchy() {
        System.out.println("\n=== Testing Hierarchical Progress Calculation ===");
        
        // Create fitness goal hierarchy
        Goal fitnessGoal = service.createHierarchicalGoal(
            "Complete Marathon Training", 
            GoalType.LONG_TERM, 
            LocalDate.of(2026, 4, 30), 
            null
        );
        
        Goal month1 = service.createHierarchicalGoal(
            "Month 1 - Base Building", 
            GoalType.MONTHLY, 
            LocalDate.of(2025, 9, 30), 
            fitnessGoal
        );
        
        Goal month2 = service.createHierarchicalGoal(
            "Month 2 - Mileage Increase", 
            GoalType.MONTHLY, 
            LocalDate.of(2025, 10, 31), 
            fitnessGoal
        );
        
        // Add tasks with different completion status
        Task task1 = new Task("Run 3 miles, 3x per week", "Build aerobic base", LocalDate.of(2025, 9, 15));
        Task task2 = new Task("Complete strength training", "2x per week", LocalDate.of(2025, 9, 30));
        task1.setCompleted(true); // Mark as completed
        task1.setStatus(Task.TaskStatus.DONE);
        
        Task task3 = new Task("Run 5 miles, 3x per week", "Increase distance", LocalDate.of(2025, 10, 15));
        Task task4 = new Task("Long run 8-10 miles", "Weekly long run", LocalDate.of(2025, 10, 31));
        task3.setCompleted(true); // Mark as completed
        task3.setStatus(Task.TaskStatus.DONE);
        
        month1.addTask(task1);
        month1.addTask(task2);
        month2.addTask(task3);
        month2.addTask(task4);
        
        // Test progress calculations
        double month1Progress = month1.getProgress();
        double month2Progress = month2.getProgress();
        double totalProgress = fitnessGoal.getProgress();
        
        Assert.assertEquals("Month 1 should be 50% complete", 50.0, month1Progress, 0.1);
        Assert.assertEquals("Month 2 should be 50% complete", 50.0, month2Progress, 0.1);
        Assert.assertEquals("Total fitness goal should be 50% complete", 50.0, totalProgress, 0.1);
        
        System.out.println("✅ Progress calculation test passed!");
        System.out.println("   📈 Month 1 progress: " + month1Progress + "%");
        System.out.println("   📈 Month 2 progress: " + month2Progress + "%");
        System.out.println("   📈 Total progress: " + totalProgress + "%");
    }
    
    @Test
    public void testHierarchyValidationRules() {
        System.out.println("\n=== Testing Hierarchy Validation Rules ===");
        
        Goal longTerm = new Goal("Long Term Goal", GoalType.LONG_TERM, LocalDate.now());
        Goal yearly = new Goal("Yearly Goal", GoalType.YEARLY, LocalDate.now());
        Goal monthly = new Goal("Monthly Goal", GoalType.MONTHLY, LocalDate.now());
        Goal weekly = new Goal("Weekly Goal", GoalType.WEEKLY, LocalDate.now());
        Goal daily = new Goal("Daily Goal", GoalType.DAILY, LocalDate.now());
        
        // Test valid relationships
        Assert.assertTrue("LONG_TERM can have YEARLY child", longTerm.canHaveChild(GoalType.YEARLY));
        Assert.assertTrue("LONG_TERM can have MONTHLY child", longTerm.canHaveChild(GoalType.MONTHLY));
        Assert.assertTrue("YEARLY can have MONTHLY child", yearly.canHaveChild(GoalType.MONTHLY));
        Assert.assertTrue("MONTHLY can have WEEKLY child", monthly.canHaveChild(GoalType.WEEKLY));
        Assert.assertTrue("WEEKLY can have DAILY child", weekly.canHaveChild(GoalType.DAILY));
        
        // Test invalid relationships
        Assert.assertFalse("LONG_TERM cannot have LONG_TERM child", longTerm.canHaveChild(GoalType.LONG_TERM));
        Assert.assertFalse("YEARLY cannot have LONG_TERM child", yearly.canHaveChild(GoalType.LONG_TERM));
        Assert.assertFalse("MONTHLY cannot have YEARLY child", monthly.canHaveChild(GoalType.YEARLY));
        Assert.assertFalse("DAILY cannot have any child", daily.canHaveChild(GoalType.WEEKLY));
        
        System.out.println("✅ Hierarchy validation rules test passed!");
    }
    
    @Test
    public void testRootGoalManagement() {
        System.out.println("\n=== Testing Root Goal Management ===");
        
        // Create multiple root goals
        Goal career = service.createHierarchicalGoal("Career Development", GoalType.LONG_TERM, LocalDate.now(), null);
        Goal health = service.createHierarchicalGoal("Health & Fitness", GoalType.YEARLY, LocalDate.now(), null);
        Goal finance = service.createHierarchicalGoal("Financial Planning", GoalType.SHORT_TERM, LocalDate.now(), null);
        
        // Create child goals
        Goal careerChild = service.createHierarchicalGoal("Learn New Technology", GoalType.MONTHLY, LocalDate.now(), career);
        Goal healthChild = service.createHierarchicalGoal("Running Program", GoalType.MONTHLY, LocalDate.now(), health);
        
        // Test root goal identification
        List<Goal> rootGoals = service.getRootGoals();
        Assert.assertEquals("Should have 3 root goals", 3, rootGoals.size());
        Assert.assertTrue("Should contain career goal", rootGoals.contains(career));
        Assert.assertTrue("Should contain health goal", rootGoals.contains(health));
        Assert.assertTrue("Should contain finance goal", rootGoals.contains(finance));
        Assert.assertFalse("Should not contain child goals", rootGoals.contains(careerChild));
        
        // Test flattened view
        List<Goal> allGoals = service.getAllGoalsFlattened();
        Assert.assertEquals("Should have 5 total goals", 5, allGoals.size());
        
        System.out.println("✅ Root goal management test passed!");
        System.out.println("   🌳 Root goals: " + rootGoals.size());
        System.out.println("   📊 Total goals: " + allGoals.size());
    }
    
    @Test
    public void testExampleHierarchyCreation() {
        System.out.println("\n=== Testing Example Hierarchy Creation ===");
        
        Goal exampleGoal = service.createExampleHierarchy();
        
        // Verify the example structure
        Assert.assertNotNull("Example goal should be created", exampleGoal);
        Assert.assertEquals("Should be long-term goal", GoalType.LONG_TERM, exampleGoal.getType());
        Assert.assertEquals("Should have correct title", "Become Senior Developer", exampleGoal.getTitle());
        
        // Check structure depth
        Assert.assertTrue("Should have sub-goals", !exampleGoal.getSubGoals().isEmpty());
        Goal yearlyGoal = exampleGoal.getSubGoals().get(0);
        Assert.assertTrue("Yearly goal should have sub-goals", !yearlyGoal.getSubGoals().isEmpty());
        Goal monthlyGoal = yearlyGoal.getSubGoals().get(0);
        Assert.assertTrue("Monthly goal should have tasks", !monthlyGoal.getTasks().isEmpty());
        
        // Verify tasks
        Assert.assertEquals("Should have 2 tasks", 2, monthlyGoal.getTasks().size());
        
        System.out.println("✅ Example hierarchy creation test passed!");
        System.out.println("   🎯 Example goal: " + exampleGoal.getTitle());
        System.out.println("   📅 Yearly goal: " + yearlyGoal.getTitle());
        System.out.println("   📆 Monthly goal: " + monthlyGoal.getTitle());
        System.out.println("   ✅ Tasks: " + monthlyGoal.getTasks().size());
    }
    
    @Test
    public void testCompleteScenario() {
        System.out.println("\n=== Testing Complete Real-World Scenario ===");
        
        // Simulate a complete user workflow
        
        // 1. Create the example hierarchy
        Goal example = service.createExampleHierarchy();
        
        // 2. Create additional personal goals
        Goal personalGoal = service.createHierarchicalGoal(
            "Personal Development 2025", 
            GoalType.YEARLY, 
            LocalDate.of(2025, 12, 31), 
            null
        );
        
        Goal readingGoal = service.createHierarchicalGoal(
            "Read 24 Books", 
            GoalType.MONTHLY, 
            LocalDate.of(2025, 12, 31), 
            personalGoal
        );
        
        // 3. Add tasks and mark some as complete
        Task readingTask1 = new Task("Read 'Clean Code'", "Software development book", LocalDate.of(2025, 8, 15));
        Task readingTask2 = new Task("Read 'System Design Interview'", "Architecture book", LocalDate.of(2025, 8, 31));
        
        readingTask1.setCompleted(true);
        readingTask1.setStatus(Task.TaskStatus.DONE);
        
        readingGoal.addTask(readingTask1);
        readingGoal.addTask(readingTask2);
        
        // 4. Test the complete system state
        List<Goal> allRootGoals = service.getRootGoals();
        List<Goal> allGoals = service.getAllGoalsFlattened();
        
        // Should have: example goal + personal goal = 2 root goals
        Assert.assertEquals("Should have 2 root goals", 2, allRootGoals.size());
        
        // Should have: multiple goals across the hierarchy
        Assert.assertTrue("Should have multiple goals", allGoals.size() >= 4);
        
        // Test progress calculation
        double personalProgress = personalGoal.getProgress();
        Assert.assertEquals("Personal goal should be 50% complete", 50.0, personalProgress, 0.1);
        
        System.out.println("✅ Complete scenario test passed!");
        System.out.println("   🌳 Root goals: " + allRootGoals.size());
        System.out.println("   📊 Total goals: " + allGoals.size());
        System.out.println("   📈 Personal development progress: " + personalProgress + "%");
        
        // Print hierarchy summary
        System.out.println("\n📋 HIERARCHY SUMMARY:");
        for (Goal root : allRootGoals) {
            printGoalHierarchy(root, 0);
        }
    }
    
    private void printGoalHierarchy(Goal goal, int level) {
        String indent = "   ".repeat(level);
        String icon = getIconForType(goal.getType());
        System.out.println(indent + icon + " " + goal.getTitle() + 
                          " (" + goal.getType() + ") - " + 
                          String.format("%.1f%%", goal.getProgress()));
        
        for (Goal subGoal : goal.getSubGoals()) {
            printGoalHierarchy(subGoal, level + 1);
        }
        
        if (level < 3 && !goal.getTasks().isEmpty()) {
            for (Task task : goal.getTasks()) {
                String taskIndent = "   ".repeat(level + 1);
                String status = task.isCompleted() ? "✅" : "⏳";
                System.out.println(taskIndent + status + " " + task.getTitle());
            }
        }
    }
    
    private String getIconForType(GoalType type) {
        switch (type) {
            case LONG_TERM: return "🎯";
            case YEARLY: return "📅";
            case MONTHLY: return "📆";
            case WEEKLY: return "📋";
            case DAILY: return "📝";
            case SHORT_TERM: return "⚡";
            default: return "📌";
        }
    }
}
