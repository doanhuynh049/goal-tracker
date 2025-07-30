package util;

import model.Goal;
import model.GoalType;
import model.Task;
import service.GoalService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for managing hierarchical goals in the Goal Tracker application.
 * Provides helper methods for creating, visualizing, and managing goal hierarchies.
 */
public class HierarchicalGoalUtil {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Creates a complete hierarchical goal structure example
     */
    public static void setupExampleHierarchy(GoalService goalService) {
        // Long-term goal for software development career
        Goal softwareCareer = goalService.createLongTermGoal(
            "Master Software Development",
            "Become an expert software developer with leadership skills",
            LocalDate.of(2028, 12, 31)
        );
        
        // Year goals for different focus areas
        Goal year2025 = goalService.createYearGoal(
            "2025 - Programming Fundamentals",
            "Master core programming languages and frameworks",
            LocalDate.of(2025, 12, 31),
            softwareCareer
        );
        
        Goal year2026 = goalService.createYearGoal(
            "2026 - System Design & Architecture",
            "Learn system design patterns and software architecture",
            LocalDate.of(2026, 12, 31),
            softwareCareer
        );
        
        Goal year2027 = goalService.createYearGoal(
            "2027 - Leadership & Management",
            "Develop team leadership and project management skills",
            LocalDate.of(2027, 12, 31),
            softwareCareer
        );
        
        // Month goals for 2025
        createMonthlyGoalsFor2025(goalService, year2025);
        
        // Month goals for 2026
        createMonthlyGoalsFor2026(goalService, year2026);
        
        // Month goals for 2027
        createMonthlyGoalsFor2027(goalService, year2027);
    }
    
    private static void createMonthlyGoalsFor2025(GoalService goalService, Goal year2025) {
        // January 2025 - Java Fundamentals
        Goal jan2025 = goalService.createMonthGoal(
            "January - Java Fundamentals",
            "Master Java basics and OOP principles",
            LocalDate.of(2025, 1, 31),
            year2025
        );
        jan2025.addTask(new Task("Complete Java OOP course", "Finish comprehensive OOP course", LocalDate.of(2025, 1, 15)));
        jan2025.addTask(new Task("Build Java calculator app", "Create a functional calculator application", LocalDate.of(2025, 1, 30)));
        
        // February 2025 - Data Structures & Algorithms
        Goal feb2025 = goalService.createMonthGoal(
            "February - Data Structures & Algorithms",
            "Learn essential data structures and algorithms",
            LocalDate.of(2025, 2, 28),
            year2025
        );
        feb2025.addTask(new Task("Study arrays, lists, and maps", "Master basic data structures", LocalDate.of(2025, 2, 15)));
        feb2025.addTask(new Task("Implement sorting algorithms", "Code bubble sort, merge sort, quick sort", LocalDate.of(2025, 2, 28)));
        
        // March 2025 - Spring Framework
        Goal mar2025 = goalService.createMonthGoal(
            "March - Spring Framework",
            "Learn Spring Boot and dependency injection",
            LocalDate.of(2025, 3, 31),
            year2025
        );
        mar2025.addTask(new Task("Spring Boot tutorial", "Complete Spring Boot getting started guide", LocalDate.of(2025, 3, 15)));
        mar2025.addTask(new Task("Build REST API", "Create a RESTful web service", LocalDate.of(2025, 3, 31)));
    }
    
    private static void createMonthlyGoalsFor2026(GoalService goalService, Goal year2026) {
        // January 2026 - Design Patterns
        Goal jan2026 = goalService.createMonthGoal(
            "January - Design Patterns",
            "Master Gang of Four design patterns",
            LocalDate.of(2026, 1, 31),
            year2026
        );
        jan2026.addTask(new Task("Study creational patterns", "Learn Singleton, Factory, Builder patterns", LocalDate.of(2026, 1, 15)));
        jan2026.addTask(new Task("Implement behavioral patterns", "Code Observer, Strategy, Command patterns", LocalDate.of(2026, 1, 31)));
        
        // February 2026 - Microservices
        Goal feb2026 = goalService.createMonthGoal(
            "February - Microservices Architecture",
            "Learn microservices design and implementation",
            LocalDate.of(2026, 2, 28),
            year2026
        );
        feb2026.addTask(new Task("Design microservices system", "Create architecture for e-commerce system", LocalDate.of(2026, 2, 15)));
        feb2026.addTask(new Task("Implement service discovery", "Set up Eureka or Consul for service discovery", LocalDate.of(2026, 2, 28)));
    }
    
    private static void createMonthlyGoalsFor2027(GoalService goalService, Goal year2027) {
        // January 2027 - Team Leadership
        Goal jan2027 = goalService.createMonthGoal(
            "January - Team Leadership",
            "Develop leadership and communication skills",
            LocalDate.of(2027, 1, 31),
            year2027
        );
        jan2027.addTask(new Task("Read leadership books", "Complete 'The Manager's Path' and 'Team Lead'", LocalDate.of(2027, 1, 15)));
        jan2027.addTask(new Task("Practice code reviews", "Conduct thorough code reviews for team", LocalDate.of(2027, 1, 31)));
        
        // February 2027 - Project Management
        Goal feb2027 = goalService.createMonthGoal(
            "February - Project Management",
            "Learn Agile and Scrum methodologies",
            LocalDate.of(2027, 2, 28),
            year2027
        );
        feb2027.addTask(new Task("Get Scrum Master certification", "Complete Scrum Master course and exam", LocalDate.of(2027, 2, 15)));
        feb2027.addTask(new Task("Lead sprint planning", "Organize and lead sprint planning meetings", LocalDate.of(2027, 2, 28)));
    }
    
    /**
     * Generates a detailed report of the goal hierarchy
     */
    public static String generateHierarchyReport(GoalService goalService) {
        StringBuilder report = new StringBuilder();
        report.append("=== HIERARCHICAL GOAL STRUCTURE REPORT ===\n\n");
        
        List<Goal> longTermGoals = goalService.getLongTermGoals();
        
        if (longTermGoals.isEmpty()) {
            report.append("No hierarchical goals found.\n");
            return report.toString();
        }
        
        for (Goal longTermGoal : longTermGoals) {
            report.append("📋 LONG-TERM GOAL: ").append(longTermGoal.getTitle()).append("\n");
            report.append("   Target Date: ").append(longTermGoal.getTargetDate()).append("\n");
            report.append("   Progress: ").append(String.format("%.1f%%", longTermGoal.getHierarchicalProgress())).append("\n");
            report.append("   Description: ").append(longTermGoal.getDescription()).append("\n\n");
            
            List<Goal> yearGoals = goalService.getYearGoalsForLongTerm(longTermGoal);
            for (Goal yearGoal : yearGoals) {
                report.append("  📅 YEAR GOAL: ").append(yearGoal.getTitle()).append("\n");
                report.append("     Target Date: ").append(yearGoal.getTargetDate()).append("\n");
                report.append("     Progress: ").append(String.format("%.1f%%", yearGoal.getHierarchicalProgress())).append("\n");
                report.append("     Description: ").append(yearGoal.getDescription()).append("\n\n");
                
                List<Goal> monthGoals = goalService.getMonthGoalsForYear(yearGoal);
                for (Goal monthGoal : monthGoals) {
                    report.append("    🗓️ MONTH GOAL: ").append(monthGoal.getTitle()).append("\n");
                    report.append("       Target Date: ").append(monthGoal.getTargetDate()).append("\n");
                    report.append("       Progress: ").append(String.format("%.1f%%", monthGoal.getProgress())).append("\n");
                    report.append("       Description: ").append(monthGoal.getDescription()).append("\n");
                    
                    List<Task> tasks = monthGoal.getTasks();
                    if (!tasks.isEmpty()) {
                        report.append("       Tasks:\n");
                        for (Task task : tasks) {
                            String status = task.isCompleted() ? "✅" : "⏳";
                            report.append("         ").append(status).append(" ").append(task.getTitle()).append("\n");
                            report.append("            Due: ").append(task.getDueDate()).append("\n");
                        }
                    }
                    report.append("\n");
                }
            }
        }
        
        return report.toString();
    }
    
    /**
     * Gets progress statistics for the entire hierarchy
     */
    public static String getProgressStatistics(GoalService goalService) {
        StringBuilder stats = new StringBuilder();
        stats.append("=== PROGRESS STATISTICS ===\n");
        
        List<Goal> longTermGoals = goalService.getLongTermGoals();
        if (longTermGoals.isEmpty()) {
            stats.append("No hierarchical goals to analyze.\n");
            return stats.toString();
        }
        
        int totalLongTermGoals = longTermGoals.size();
        int totalYearGoals = 0;
        int totalMonthGoals = 0;
        int totalTasks = 0;
        int completedTasks = 0;
        
        for (Goal longTermGoal : longTermGoals) {
            List<Goal> yearGoals = goalService.getYearGoalsForLongTerm(longTermGoal);
            totalYearGoals += yearGoals.size();
            
            for (Goal yearGoal : yearGoals) {
                List<Goal> monthGoals = goalService.getMonthGoalsForYear(yearGoal);
                totalMonthGoals += monthGoals.size();
                
                for (Goal monthGoal : monthGoals) {
                    List<Task> tasks = monthGoal.getTasks();
                    totalTasks += tasks.size();
                    completedTasks += (int) tasks.stream().filter(Task::isCompleted).count();
                }
            }
        }
        
        stats.append("Long-term Goals: ").append(totalLongTermGoals).append("\n");
        stats.append("Year Goals: ").append(totalYearGoals).append("\n");
        stats.append("Month Goals: ").append(totalMonthGoals).append("\n");
        stats.append("Total Tasks: ").append(totalTasks).append("\n");
        stats.append("Completed Tasks: ").append(completedTasks).append("\n");
        
        if (totalTasks > 0) {
            double completionRate = (double) completedTasks / totalTasks * 100;
            stats.append("Overall Completion Rate: ").append(String.format("%.1f%%", completionRate)).append("\n");
        }
        
        return stats.toString();
    }
}
