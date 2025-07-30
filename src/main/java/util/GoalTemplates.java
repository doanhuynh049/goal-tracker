package util;

import model.Goal;
import model.GoalType;
import model.Task;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Goal Templates Utility for creating predefined goal structures
 */
public class GoalTemplates {
    
    public static class Template {
        private final String name;
        private final String description;
        private final GoalType type;
        private final List<String> taskTitles;
        private final int estimatedDaysToComplete;
        
        public Template(String name, String description, GoalType type, 
                       List<String> taskTitles, int estimatedDaysToComplete) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.taskTitles = taskTitles;
            this.estimatedDaysToComplete = estimatedDaysToComplete;
        }
        
        public Goal createGoal() {
            Goal goal = new Goal(
                name,
                description,
                type,
                LocalDate.now().plusDays(estimatedDaysToComplete),
                null
            );
            
            // Add template tasks
            for (String taskTitle : taskTitles) {
                Task task = new Task(taskTitle, "", null);
                task.setPriority(Task.Priority.MEDIUM);
                task.setStatus(Task.TaskStatus.TO_DO);
                goal.addTask(task);
            }
            
            return goal;
        }
        
        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public GoalType getType() { return type; }
        public List<String> getTaskTitles() { return taskTitles; }
        public int getEstimatedDaysToComplete() { return estimatedDaysToComplete; }
    }
    
    private static final List<Template> TEMPLATES = Arrays.asList(
        // Fitness Templates
        new Template(
            "🏃‍♂️ 30-Day Fitness Challenge",
            "Complete a comprehensive 30-day fitness transformation",
            GoalType.MONTHLY,
            Arrays.asList(
                "Create workout schedule",
                "Set up nutrition plan",
                "Week 1: Basic cardio and strength",
                "Week 2: Increase intensity",
                "Week 3: Add new exercises",
                "Week 4: Final challenge week",
                "Take progress photos",
                "Measure fitness improvements"
            ),
            30
        ),
        
        // Learning Templates
        new Template(
            "📚 Learn New Programming Language",
            "Master a new programming language in 3 months",
            GoalType.LONG_TERM,
            Arrays.asList(
                "Choose programming language",
                "Find quality learning resources",
                "Set up development environment",
                "Complete basic syntax tutorial",
                "Build first simple project",
                "Learn advanced concepts",
                "Create portfolio project",
                "Join programming community",
                "Contribute to open source project"
            ),
            90
        ),
        
        // Career Templates
        new Template(
            "💼 Career Advancement Plan",
            "Advance in current career within 6 months",
            GoalType.LONG_TERM,
            Arrays.asList(
                "Assess current skills and gaps",
                "Set specific promotion goals",
                "Schedule meeting with manager",
                "Identify required certifications",
                "Complete relevant training courses",
                "Take on leadership projects",
                "Build professional network",
                "Update resume and LinkedIn",
                "Prepare for performance review"
            ),
            180
        ),
        
        // Financial Templates
        new Template(
            "💰 Build Emergency Fund",
            "Save 6 months of expenses as emergency fund",
            GoalType.YEARLY,
            Arrays.asList(
                "Calculate monthly expenses",
                "Set savings target amount",
                "Open dedicated savings account",
                "Set up automatic transfers",
                "Track monthly progress",
                "Reduce unnecessary expenses",
                "Find additional income sources",
                "Review and adjust plan quarterly"
            ),
            365
        ),
        
        // Health Templates
        new Template(
            "🍎 Healthy Lifestyle Transformation",
            "Adopt a comprehensive healthy lifestyle",
            GoalType.LONG_TERM,
            Arrays.asList(
                "Schedule health checkup",
                "Create meal planning system",
                "Establish exercise routine",
                "Improve sleep schedule",
                "Reduce stress levels",
                "Quit bad habits",
                "Build healthy morning routine",
                "Track health metrics",
                "Join health-focused community"
            ),
            120
        ),
        
        // Project Templates
        new Template(
            "🚀 Launch Side Business",
            "Start and launch a profitable side business",
            GoalType.YEARLY,
            Arrays.asList(
                "Research business ideas",
                "Validate business concept",
                "Create business plan",
                "Register business legally",
                "Set up website and branding",
                "Develop MVP product/service",
                "Launch marketing campaign",
                "Acquire first 10 customers",
                "Optimize and scale operations"
            ),
            365
        ),
        
        // Personal Development Templates
        new Template(
            "🧘‍♀️ Mindfulness & Self-Care",
            "Develop consistent mindfulness and self-care practices",
            GoalType.MONTHLY,
            Arrays.asList(
                "Learn meditation techniques",
                "Create daily meditation practice",
                "Start gratitude journaling",
                "Schedule regular self-care time",
                "Reduce social media usage",
                "Practice mindful eating",
                "Develop stress management techniques",
                "Create positive morning routine"
            ),
            60
        ),
        
        // Travel Templates
        new Template(
            "✈️ Plan Dream Vacation",
            "Plan and execute the perfect vacation experience",
            GoalType.SHORT_TERM,
            Arrays.asList(
                "Choose destination",
                "Set travel budget",
                "Research accommodations",
                "Book flights and hotels",
                "Plan daily itinerary",
                "Arrange travel documents",
                "Pack efficiently",
                "Create travel memories album"
            ),
            90
        ),
        
        // Home & Organization Templates
        new Template(
            "🏠 Home Organization Project",
            "Completely organize and optimize living space",
            GoalType.MONTHLY,
            Arrays.asList(
                "Assess current organization level",
                "Declutter each room systematically",
                "Donate or sell unused items",
                "Create storage solutions",
                "Label everything properly",
                "Establish maintenance routines",
                "Optimize room layouts",
                "Create cleaning schedule"
            ),
            45
        ),
        
        // Relationship Templates
        new Template(
            "❤️ Strengthen Relationships",
            "Improve and deepen important relationships",
            GoalType.LONG_TERM,
            Arrays.asList(
                "Identify key relationships to focus on",
                "Schedule regular quality time",
                "Improve communication skills",
                "Practice active listening",
                "Plan special activities together",
                "Express appreciation regularly",
                "Resolve any ongoing conflicts",
                "Create new shared experiences"
            ),
            120
        )
    );
    
    /**
     * Get all available templates
     */
    public static List<Template> getAllTemplates() {
        return new ArrayList<>(TEMPLATES);
    }
    
    /**
     * Get templates by goal type
     */
    public static List<Template> getTemplatesByType(GoalType type) {
        return TEMPLATES.stream()
            .filter(template -> template.getType() == type)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Get template by name
     */
    public static Template getTemplateByName(String name) {
        return TEMPLATES.stream()
            .filter(template -> template.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Search templates by keyword
     */
    public static List<Template> searchTemplates(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return TEMPLATES.stream()
            .filter(template -> 
                template.getName().toLowerCase().contains(lowerKeyword) ||
                template.getDescription().toLowerCase().contains(lowerKeyword) ||
                template.getTaskTitles().stream()
                    .anyMatch(task -> task.toLowerCase().contains(lowerKeyword))
            )
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Get featured templates (most popular/recommended)
     */
    public static List<Template> getFeaturedTemplates() {
        return Arrays.asList(
            getTemplateByName("🏃‍♂️ 30-Day Fitness Challenge"),
            getTemplateByName("📚 Learn New Programming Language"),
            getTemplateByName("💼 Career Advancement Plan"),
            getTemplateByName("💰 Build Emergency Fund"),
            getTemplateByName("🚀 Launch Side Business")
        );
    }
    
    /**
     * Create a custom template
     */
    public static Template createCustomTemplate(String name, String description, 
                                              GoalType type, List<String> tasks, 
                                              int estimatedDays) {
        return new Template(name, description, type, tasks, estimatedDays);
    }
}
