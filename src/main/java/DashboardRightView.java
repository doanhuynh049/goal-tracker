import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Goal;
import model.Task;
import service.GoalService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DashboardView provides an enhanced dashboard interface for the Goal & Task Manager
 * with overview cards, today's focus, recent activity, quick actions, and progress charts
 */
public class DashboardRightView {
    
    private final GoalService goalService;
    private final MainView mainView;
    private final boolean isDarkTheme;
    
    public DashboardRightView(GoalService goalService, MainView mainView, boolean isDarkTheme) {
        this.goalService = goalService;
        this.mainView = mainView;
        this.isDarkTheme = isDarkTheme;
    }

       // Create enhanced dashboard with key metrics, today's focus, recent activity, and quick actions
    public VBox createEnhancedDashboard() {
        VBox dashboardLayout = new VBox(20);
        dashboardLayout.setPadding(new Insets(20));
        updateLayoutTheme(dashboardLayout);
        
        // Create scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox scrollContent = new VBox(25);
        scrollContent.setPadding(new Insets(10));
        
        // Dashboard title
        Label dashboardTitle = new Label("📊 Dashboard Overview");
        dashboardTitle.setStyle(getDashboardTitleStyle());
        
        // Overview Cards Section
        HBox overviewCards = createOverviewCards();
        
        // Today's Focus Section
        VBox todaysFocus = createTodaysFocusSection();
        
        // Recent Activity Section
        VBox recentActivity = createRecentActivitySection();
        
        // Quick Actions Section
        VBox quickActions = createQuickActionsSection();
        
        // Progress Charts Section
        VBox progressCharts = createProgressChartsSection();
        
        scrollContent.getChildren().addAll(
            dashboardTitle,
            overviewCards,
            todaysFocus,
            recentActivity,
            quickActions,
            progressCharts
        );
        
        scrollPane.setContent(scrollContent);
        dashboardLayout.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        return dashboardLayout;
    }
    
    // Create overview cards with key metrics
    private HBox createOverviewCards() {
        HBox cardsContainer = new HBox(15);
        cardsContainer.setAlignment(Pos.CENTER);
        
        // Total Goals Card
        int totalGoals = goalService.getAllGoals().size();
        VBox goalCard = createMetricCard("🎯", "Total Goals", String.valueOf(totalGoals), "#3498db");
        
        // Active Goals Card
        long activeGoals = goalService.getAllGoals().stream()
            .filter(goal -> goal.getProgress() < 100.0)
            .count();
        VBox activeGoalCard = createMetricCard("🔥", "Active Goals", String.valueOf(activeGoals), "#e67e22");
        
        // Total Tasks Card
        int totalTasks = goalService.getAllGoals().stream()
            .mapToInt(goal -> goal.getTasks().size())
            .sum();
        VBox taskCard = createMetricCard("📝", "Total Tasks", String.valueOf(totalTasks), "#9b59b6");
        
        // Completed Tasks Card
        long completedTasks = goalService.getAllGoals().stream()
            .flatMap(goal -> goal.getTasks().stream())
            .filter(task -> task.getStatus() == model.Task.TaskStatus.DONE)
            .count();
        VBox completedCard = createMetricCard("✅", "Completed", String.valueOf(completedTasks), "#27ae60");
        
        cardsContainer.getChildren().addAll(goalCard, activeGoalCard, taskCard, completedCard);
        return cardsContainer;
    }
    
    // Create individual metric card
    private VBox createMetricCard(String icon, String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle(getMetricCardStyle());
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle(getCardTitleStyle());
        
        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        
        // Add hover effect
        card.setOnMouseEntered(e -> card.setStyle(getMetricCardHoverStyle()));
        card.setOnMouseExited(e -> card.setStyle(getMetricCardStyle()));
        
        return card;
    }
    
    // Create Today's Focus section
    private VBox createTodaysFocusSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("🎯 Today's Focus");
        sectionTitle.setStyle(getSectionTitleStyle());
        
        VBox focusContainer = new VBox(10);
        focusContainer.setPadding(new Insets(20));
        focusContainer.setStyle(getSectionCardStyle());
        
        // Get today's urgent and high priority tasks
        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<model.Task> urgentTasks = goalService.getAllGoals().stream()
            .flatMap(goal -> goal.getTasks().stream())
            .filter(task -> task.getStatus() != model.Task.TaskStatus.DONE)
            .filter(task -> task.getPriority() == model.Task.Priority.URGENT || 
                          task.getPriority() == model.Task.Priority.HIGH ||
                          (task.getDueDate() != null && task.getDueDate().equals(today)))
            .sorted((t1, t2) -> {
                // Sort by priority first, then by due date
                int priorityCompare = t1.getPriority().compareTo(t2.getPriority());
                if (priorityCompare != 0) return priorityCompare;
                if (t1.getDueDate() != null && t2.getDueDate() != null) {
                    return t1.getDueDate().compareTo(t2.getDueDate());
                }
                return 0;
            })
            .limit(5)
            .collect(java.util.stream.Collectors.toList());
        
        if (urgentTasks.isEmpty()) {
            Label noTasksLabel = new Label("🎉 No urgent tasks today! Great job!");
            noTasksLabel.setStyle(getNoTasksStyle());
            focusContainer.getChildren().add(noTasksLabel);
        } else {
            for (model.Task task : urgentTasks) {
                HBox taskItem = createTodayTaskItem(task);
                focusContainer.getChildren().add(taskItem);
            }
        }
        
        section.getChildren().addAll(sectionTitle, focusContainer);
        return section;
    }
    
    // Create task item for today's focus
    private HBox createTodayTaskItem(model.Task task) {
        HBox taskItem = new HBox(10);
        taskItem.setAlignment(Pos.CENTER_LEFT);
        taskItem.setPadding(new Insets(8));
        taskItem.setStyle(getTaskItemStyle());
        
        // Priority indicator
        String priorityIcon = getPriorityIcon(task.getPriority());
        Label priorityLabel = new Label(priorityIcon);
        priorityLabel.setStyle("-fx-font-size: 16px;");
        
        // Task title
        Label taskTitle = new Label(task.getTitle());
        taskTitle.setStyle(getTaskTitleStyle());
        
        // Due date indicator
        Label dueDateLabel = new Label();
        if (task.getDueDate() != null) {
            boolean isOverdue = task.getDueDate().isBefore(java.time.LocalDate.now());
            boolean isDueToday = task.getDueDate().equals(java.time.LocalDate.now());
            
            if (isOverdue) {
                dueDateLabel.setText("⚠️ Overdue");
                dueDateLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else if (isDueToday) {
                dueDateLabel.setText("📅 Due Today");
                dueDateLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            } else {
                dueDateLabel.setText("📅 " + task.getDueDate().toString());
                dueDateLabel.setStyle(getSecondaryTextStyle());
            }
        }
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        taskItem.getChildren().addAll(priorityLabel, taskTitle, spacer, dueDateLabel);
        
        // Hover effect
        taskItem.setOnMouseEntered(e -> taskItem.setStyle(getTaskItemHoverStyle()));
        taskItem.setOnMouseExited(e -> taskItem.setStyle(getTaskItemStyle()));
        
        return taskItem;
    }
    
    // Create Recent Activity section
    private VBox createRecentActivitySection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("🕒 Recent Activity");
        sectionTitle.setStyle(getSectionTitleStyle());
        
        VBox activityContainer = new VBox(8);
        activityContainer.setPadding(new Insets(20));
        activityContainer.setStyle(getSectionCardStyle());
        
        // Get recent completed tasks (last 5)
        java.util.List<model.Task> recentTasks = goalService.getAllGoals().stream()
            .flatMap(goal -> goal.getTasks().stream())
            .filter(task -> task.getStatus() == model.Task.TaskStatus.DONE)
            .sorted((t1, t2) -> {
                // Sort by completion date if available, otherwise by creation order
                if (t1.getCompletedDate() != null && t2.getCompletedDate() != null) {
                    return t2.getCompletedDate().compareTo(t1.getCompletedDate());
                }
                return 0;
            })
            .limit(5)
            .collect(java.util.stream.Collectors.toList());
        
        if (recentTasks.isEmpty()) {
            Label noActivityLabel = new Label("No recent activity. Start completing some tasks!");
            noActivityLabel.setStyle(getSecondaryTextStyle());
            activityContainer.getChildren().add(noActivityLabel);
        } else {
            for (model.Task task : recentTasks) {
                HBox activityItem = createActivityItem(task);
                activityContainer.getChildren().add(activityItem);
            }
        }
        
        // Add recent goals created
        java.util.List<model.Goal> recentGoals = goalService.getAllGoals().stream()
            .sorted((g1, g2) -> {
                if (g1.getLastUpdated() != null && g2.getLastUpdated() != null) {
                    return g2.getLastUpdated().compareTo(g1.getLastUpdated());
                }
                return 0;
            })
            .limit(2)
            .collect(java.util.stream.Collectors.toList());
        
        for (model.Goal goal : recentGoals) {
            HBox goalActivity = createGoalActivityItem(goal);
            activityContainer.getChildren().add(goalActivity);
        }
        
        section.getChildren().addAll(sectionTitle, activityContainer);
        return section;
    }
    
    // Create activity item for completed task
    private HBox createActivityItem(model.Task task) {
        HBox activityItem = new HBox(10);
        activityItem.setAlignment(Pos.CENTER_LEFT);
        activityItem.setPadding(new Insets(5));
        
        Label icon = new Label("✅");
        icon.setStyle("-fx-font-size: 14px;");
        
        Label description = new Label("Completed: " + task.getTitle());
        description.setStyle(getActivityTextStyle());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label timeLabel = new Label("Recently");
        timeLabel.setStyle(getTimeStampStyle());
        
        activityItem.getChildren().addAll(icon, description, spacer, timeLabel);
        return activityItem;
    }
    
    // Create activity item for goal
    private HBox createGoalActivityItem(model.Goal goal) {
        HBox activityItem = new HBox(10);
        activityItem.setAlignment(Pos.CENTER_LEFT);
        activityItem.setPadding(new Insets(5));
        
        Label icon = new Label("🎯");
        icon.setStyle("-fx-font-size: 14px;");
        
        Label description = new Label("Updated goal: " + goal.getTitle());
        description.setStyle(getActivityTextStyle());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label timeLabel = new Label("Recently");
        timeLabel.setStyle(getTimeStampStyle());
        
        activityItem.getChildren().addAll(icon, description, spacer, timeLabel);
        return activityItem;
    }
    
    // Create Quick Actions section
    private VBox createQuickActionsSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("⚡ Quick Actions");
        sectionTitle.setStyle(getSectionTitleStyle());
        
        HBox actionsContainer = new HBox(15);
        actionsContainer.setAlignment(Pos.CENTER);
        actionsContainer.setPadding(new Insets(20));
        actionsContainer.setStyle(getSectionCardStyle());
        
        // Quick action buttons
        Button createGoalBtn = createQuickActionButton("🎯", "Create Goal", "#3498db");
        createGoalBtn.setOnAction(e -> createGoal());
        
        Button addTaskBtn = createQuickActionButton("📝", "Add Task", "#27ae60");
        addTaskBtn.setOnAction(e -> addTask());
        
        Button viewGoalsBtn = createQuickActionButton("👀", "View Goals", "#9b59b6");
        viewGoalsBtn.setOnAction(e -> viewGoals());
        
        Button statsBtn = createQuickActionButton("📊", "Statistics", "#e67e22");
        statsBtn.setOnAction(e -> viewStatistics());
        
        actionsContainer.getChildren().addAll(createGoalBtn, addTaskBtn, viewGoalsBtn, statsBtn);
        
        VBox fullSection = new VBox(15);
        fullSection.getChildren().addAll(sectionTitle, actionsContainer);
        
        return fullSection;
    }
    
    // Create quick action button
    private Button createQuickActionButton(String icon, String text, String color) {
        Button button = new Button();
        button.setPrefSize(120, 80);
        button.setStyle(getQuickActionButtonStyle(color));
        
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        content.getChildren().addAll(iconLabel, textLabel);
        button.setGraphic(content);
        
        // Hover effects
        button.setOnMouseEntered(e -> button.setStyle(getQuickActionButtonHoverStyle(color)));
        button.setOnMouseExited(e -> button.setStyle(getQuickActionButtonStyle(color)));
        
        return button;
    }
    
    // Create Progress Charts section (mini versions)
    private VBox createProgressChartsSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("📈 Progress Overview");
        sectionTitle.setStyle(getSectionTitleStyle());
        
        HBox chartsContainer = new HBox(20);
        chartsContainer.setAlignment(Pos.CENTER);
        chartsContainer.setPadding(new Insets(20));
        chartsContainer.setStyle(getSectionCardStyle());
        
        // Goal completion mini chart
        VBox goalChart = createMiniGoalChart();
        
        // Task priority distribution mini chart
        VBox taskChart = createMiniTaskChart();
        
        chartsContainer.getChildren().addAll(goalChart, taskChart);
        section.getChildren().addAll(sectionTitle, chartsContainer);
        
        return section;
    }
    
    // Create mini goal completion chart
    private VBox createMiniGoalChart() {
        VBox chartContainer = new VBox(10);
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.setPrefWidth(250);
        
        Label chartTitle = new Label("Goal Completion");
        chartTitle.setStyle(getChartTitleStyle());
        
        // Calculate completion stats
        java.util.List<model.Goal> goals = goalService.getAllGoals();
        long completedGoals = goals.stream().filter(g -> g.getProgress() >= 100.0).count();
        long inProgressGoals = goals.stream().filter(g -> g.getProgress() > 0 && g.getProgress() < 100.0).count();
        long notStartedGoals = goals.stream().filter(g -> g.getProgress() == 0).count();
        
        // Create simple progress bars
        VBox barsContainer = new VBox(5);
        
        HBox completedBar = createMiniProgressBar("Completed", completedGoals, goals.size(), "#27ae60");
        HBox inProgressBar = createMiniProgressBar("In Progress", inProgressGoals, goals.size(), "#f39c12");
        HBox notStartedBar = createMiniProgressBar("Not Started", notStartedGoals, goals.size(), "#95a5a6");
        
        barsContainer.getChildren().addAll(completedBar, inProgressBar, notStartedBar);
        chartContainer.getChildren().addAll(chartTitle, barsContainer);
        
        return chartContainer;
    }
    
    // Create mini task priority chart
    private VBox createMiniTaskChart() {
        VBox chartContainer = new VBox(10);
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.setPrefWidth(250);
        
        Label chartTitle = new Label("Task Priorities");
        chartTitle.setStyle(getChartTitleStyle());
        
        // Calculate task priority stats
        java.util.List<model.Task> allTasks = goalService.getAllGoals().stream()
            .flatMap(goal -> goal.getTasks().stream())
            .filter(task -> task.getStatus() != model.Task.TaskStatus.DONE)
            .collect(java.util.stream.Collectors.toList());
        
        long urgentTasks = allTasks.stream().filter(t -> t.getPriority() == model.Task.Priority.URGENT).count();
        long highTasks = allTasks.stream().filter(t -> t.getPriority() == model.Task.Priority.HIGH).count();
        long mediumTasks = allTasks.stream().filter(t -> t.getPriority() == model.Task.Priority.MEDIUM).count();
        long lowTasks = allTasks.stream().filter(t -> t.getPriority() == model.Task.Priority.LOW).count();
        
        VBox barsContainer = new VBox(5);
        
        HBox urgentBar = createMiniProgressBar("Urgent", urgentTasks, allTasks.size(), "#e74c3c");
        HBox highBar = createMiniProgressBar("High", highTasks, allTasks.size(), "#e67e22");
        HBox mediumBar = createMiniProgressBar("Medium", mediumTasks, allTasks.size(), "#f1c40f");
        HBox lowBar = createMiniProgressBar("Low", lowTasks, allTasks.size(), "#27ae60");
        
        barsContainer.getChildren().addAll(urgentBar, highBar, mediumBar, lowBar);
        chartContainer.getChildren().addAll(chartTitle, barsContainer);
        
        return chartContainer;
    }
    
    // Create mini progress bar
    private HBox createMiniProgressBar(String label, long value, int total, String color) {
        HBox barContainer = new HBox(10);
        barContainer.setAlignment(Pos.CENTER_LEFT);
        
        Label labelText = new Label(label);
        labelText.setPrefWidth(80);
        labelText.setStyle(getBarLabelStyle());
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(120);
        progressBar.setPrefHeight(8);
        progressBar.setProgress(total > 0 ? (double) value / total : 0.0);
        progressBar.setStyle("-fx-accent: " + color + ";");
        
        Label valueText = new Label(value + "/" + total);
        valueText.setStyle(getBarValueStyle());
        
        barContainer.getChildren().addAll(labelText, progressBar, valueText);
        return barContainer;
    }
    
    // Dashboard styling methods
    private String getDashboardTitleStyle() {
        return isDarkTheme ?
            "-fx-font-size: 32px; -fx-text-fill: #ecf0f1; -fx-font-weight: bold; -fx-padding: 0 0 20 0;" :
            "-fx-font-size: 32px; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-padding: 0 0 20 0;";
    }
    
    private String getMetricCardStyle() {
        return isDarkTheme ?
            "-fx-background-color: #34495e; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.1, 0, 2);" :
            "-fx-background-color: white; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(44,62,80,0.1), 8, 0.1, 0, 2);";
    }
    
    private String getMetricCardHoverStyle() {
        return isDarkTheme ?
            "-fx-background-color: #3e5770; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0.2, 0, 4);" :
            "-fx-background-color: #f8f9fa; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(44,62,80,0.15), 12, 0.2, 0, 4);";
    }
    
    private String getCardTitleStyle() {
        return isDarkTheme ?
            "-fx-font-size: 14px; -fx-text-fill: #bdc3c7; -fx-font-weight: normal;" :
            "-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-weight: normal;";
    }
    
    private String getSectionTitleStyle() {
        return isDarkTheme ?
            "-fx-font-size: 20px; -fx-text-fill: #ecf0f1; -fx-font-weight: bold;" :
            "-fx-font-size: 20px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;";
    }
    
    private String getSectionCardStyle() {
        return isDarkTheme ?
            "-fx-background-color: #34495e; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0.1, 0, 2);" :
            "-fx-background-color: white; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(44,62,80,0.08), 6, 0.1, 0, 2);";
    }
    
    private String getNoTasksStyle() {
        return isDarkTheme ?
            "-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-style: italic;" :
            "-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-style: italic;";
    }
    
    private String getTaskItemStyle() {
        return isDarkTheme ?
            "-fx-background-color: transparent; -fx-background-radius: 6px;" :
            "-fx-background-color: transparent; -fx-background-radius: 6px;";
    }
    
    private String getTaskItemHoverStyle() {
        return isDarkTheme ?
            "-fx-background-color: rgba(52, 73, 94, 0.5); -fx-background-radius: 6px;" :
            "-fx-background-color: rgba(236, 240, 241, 0.5); -fx-background-radius: 6px;";
    }
    
    private String getTaskTitleStyle() {
        return isDarkTheme ?
            "-fx-font-size: 14px; -fx-text-fill: #ecf0f1; -fx-font-weight: bold;" :
            "-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;";
    }
    
    private String getSecondaryTextStyle() {
        return isDarkTheme ?
            "-fx-font-size: 12px; -fx-text-fill: #95a5a6;" :
            "-fx-font-size: 12px; -fx-text-fill: #7f8c8d;";
    }
    
    private String getActivityTextStyle() {
        return isDarkTheme ?
            "-fx-font-size: 13px; -fx-text-fill: #bdc3c7;" :
            "-fx-font-size: 13px; -fx-text-fill: #2c3e50;";
    }
    
    private String getTimeStampStyle() {
        return isDarkTheme ?
            "-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-font-style: italic;" :
            "-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;";
    }
    
    private String getQuickActionButtonStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-background-radius: 8px; " +
               "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0.1, 0, 2);";
    }
    
    private String getQuickActionButtonHoverStyle(String color) {
        return "-fx-background-color: derive(" + color + ", -10%); -fx-background-radius: 8px; " +
               "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0.2, 0, 4);";
    }
    
    private String getChartTitleStyle() {
        return isDarkTheme ?
            "-fx-font-size: 16px; -fx-text-fill: #ecf0f1; -fx-font-weight: bold;" :
            "-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;";
    }
    
    private String getBarLabelStyle() {
        return isDarkTheme ?
            "-fx-font-size: 12px; -fx-text-fill: #bdc3c7;" :
            "-fx-font-size: 12px; -fx-text-fill: #2c3e50;";
    }
    
    private String getBarValueStyle() {
        return isDarkTheme ?
            "-fx-font-size: 11px; -fx-text-fill: #95a5a6;" :
            "-fx-font-size: 11px; -fx-text-fill: #7f8c8d;";
    }
    
    // Helper method to get priority icon
    private String getPriorityIcon(model.Task.Priority priority) {
        switch (priority) {
            case URGENT: return "🔴";
            case HIGH: return "🟠";
            case MEDIUM: return "🟡";
            case LOW: return "🟢";
            default: return "⚪";
        }
    }

    private void updateLayoutTheme(VBox layout) {
        String backgroundStyle = isDarkTheme ?
            "-fx-background-color: linear-gradient(to bottom right, #2c3e50, #34495e);" :
            "-fx-background-color: linear-gradient(to bottom right, #e0eafc, #cfdef3);";
        layout.setStyle(backgroundStyle);
    }

    private void createGoal() {
        mainView.createGoal();
    }

    private void addTask() {
        mainView.addTask();
    }

    private void viewGoals() {
        mainView.viewGoals();
    }

    private void viewStatistics() {
        mainView.viewStatistics();
    }
}
