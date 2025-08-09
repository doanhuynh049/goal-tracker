import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import model.Goal;
import model.Task;
import service.GoalService;
import util.AppLogger;

import java.time.LocalDate;
import java.util.List;

/**
 * GoalsRightView provides an enhanced goals interface for the Goal & Task Manager
 * to be displayed as the right panel in MainView instead of opening a new window.
 * This replaces the traditional GoalsView popup with an integrated panel approach.
 */
public class GoalsRightView {
    private static final java.util.logging.Logger logger = AppLogger.getLogger();
    
    private final GoalService goalService;
    private final MainView mainView;
    private final boolean isDarkTheme;
    private VBox leftColumn;
    private VBox rightColumn;
    private ScrollPane leftScrollPane; // Store reference to toggle visibility
    private boolean hierarchyVisible = true; // Track visibility state
    private Goal currentlySelectedGoal; // Track the currently selected goal
    
    public GoalsRightView(GoalService goalService, MainView mainView, boolean isDarkTheme) {
        this.goalService = goalService;
        this.mainView = mainView;
        this.isDarkTheme = isDarkTheme;
        logger.info("GoalsRightView initialized");
    }
    
    /**
     * Creates and returns the complete goals view panel with left column (goals list) 
     * and right column (selected goal's tasks)
     */
    public HBox createGoalsView() {
        logger.info("Creating GoalsRightView panel");
        
        // Create main container
        HBox goalsContainer = new HBox(20);
        goalsContainer.setPadding(new Insets(20));
        updateLayoutTheme(goalsContainer);
        
        // Create left column (goals list)
        VBox leftPanel = createLeftPanel();
        
        // Create right column (task details)
        VBox rightPanel = createRightPanel();
        
        // Set up scroll panes for both panels
        leftScrollPane = new ScrollPane(leftPanel);
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setFitToHeight(true);
        leftScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        leftScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScrollPane.setPrefWidth(300);
        leftScrollPane.setStyle("-fx-background-color: transparent;");
        
        ScrollPane rightScrollPane = new ScrollPane(rightPanel);
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setFitToHeight(true);
        rightScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rightScrollPane.setStyle("-fx-background-color: transparent;");
        
        // Add panels to container
        HBox.setHgrow(rightScrollPane, Priority.ALWAYS);
        goalsContainer.getChildren().addAll(leftScrollPane, rightScrollPane);
        
        return goalsContainer;
    }
    
    /**
     * Creates the left panel containing the hierarchical goals list
     */
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setStyle(getLeftPanelStyle());
        
        // Panel title
        Label title = new Label("🎯 Goals Hierarchy");
        title.setStyle(getSectionTitleStyle());
        
        // Goals list container
        leftColumn = new VBox(8);
        updateLeftColumn();
        
        // Save button
        Button saveButton = new Button("💾 Save to Excel");
        saveButton.setStyle(getActionButtonStyle());
        saveButton.setOnAction(e -> {
            goalService.saveGoalsToFile();
            showInfoMessage("Data saved to Excel successfully!");
        });
        
        leftPanel.getChildren().addAll(title, new Separator(), leftColumn, new Separator(), saveButton);
        VBox.setVgrow(leftColumn, Priority.ALWAYS);
        
        return leftPanel;
    }
    
    /**
     * Creates the right panel for displaying selected goal's tasks
     */
    private VBox createRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setStyle(getRightPanelStyle());
        
        // Panel header with title and toggle button
        HBox panelHeader = new HBox(10);
        panelHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("📋 Goal Details & Tasks");
        title.setStyle(getSectionTitleStyle());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Toggle hierarchy button
        Button toggleHierarchyBtn = new Button(hierarchyVisible ? "👁️ Hide Hierarchy" : "👁️ Show Hierarchy");
        toggleHierarchyBtn.setStyle(getToggleButtonStyle());
        toggleHierarchyBtn.setOnAction(e -> toggleHierarchyVisibility(toggleHierarchyBtn));
        
        panelHeader.getChildren().addAll(title, spacer, toggleHierarchyBtn);
        
        // Task details container
        rightColumn = new VBox(10);
        
        // Show tasks of the first goal if available
        List<Goal> allGoals = goalService.getAllGoals();
        if (!allGoals.isEmpty()) {
            Goal firstGoal = allGoals.get(0);
            this.currentlySelectedGoal = firstGoal;
            updateRightColumn(firstGoal);
        } else {
            rightColumn.getChildren().add(createEmptyStateMessage());
        }
        
        rightPanel.getChildren().addAll(panelHeader, new Separator(), rightColumn);
        VBox.setVgrow(rightColumn, Priority.ALWAYS);
        
        return rightPanel;
    }
    
    /**
     * Updates the left column with hierarchical goals display
     */
    private void updateLeftColumn() {
        if (leftColumn == null) return;
        logger.info("Refreshing goals list");
        leftColumn.getChildren().clear();
        
        // Get root goals and display them hierarchically
        List<Goal> rootGoals = goalService.getRootGoals();
        
        // Sort root goals by type, then by due date
        rootGoals.sort((g1, g2) -> {
            int typeCompare = g1.getType().compareTo(g2.getType());
            if (typeCompare != 0) return typeCompare;
            if (g1.getTargetDate() != null && g2.getTargetDate() != null) {
                return g1.getTargetDate().compareTo(g2.getTargetDate());
            } else if (g1.getTargetDate() == null) {
                return 1;
            } else if (g2.getTargetDate() == null) {
                return -1;
            }
            return 0;
        });
        
        // Display goals hierarchically
        for (Goal rootGoal : rootGoals) {
            addGoalToColumn(rootGoal, 0);
        }
        
        if (rootGoals.isEmpty()) {
            leftColumn.getChildren().add(createEmptyGoalsMessage());
        }
    }
    
    /**
     * Adds a goal to the left column with proper indentation for hierarchy and enhanced visual indicators
     */
    private void addGoalToColumn(Goal goal, int indentLevel) {
        // Create goal container with visual hierarchy
        VBox goalContainer = new VBox(5);
        goalContainer.setPadding(new Insets(5, 5, 5, 10 + (indentLevel * 15)));
        
        // Create goal button with hierarchy indicator
        String indentString = "  ".repeat(indentLevel);
        String hierarchyPrefix = getHierarchyPrefix(indentLevel, goal);
        String goalText = indentString + hierarchyPrefix + goal.getTitle();
        
        Button goalButton = new Button(goalText);
        goalButton.setMaxWidth(Double.MAX_VALUE);
        goalButton.setAlignment(Pos.CENTER_LEFT);
        goalButton.setStyle(getGoalButtonStyle(goal));
        
        // Add click handler to show goal details
        goalButton.setOnAction(e -> updateRightColumn(goal));
        
        // Add hover effects
        goalButton.setOnMouseEntered(e -> goalButton.setStyle(getGoalButtonHoverStyle()));
        goalButton.setOnMouseExited(e -> goalButton.setStyle(getGoalButtonStyle(goal)));
        
        // Add progress indicator below goal name
        HBox progressRow = new HBox(5);
        progressRow.setAlignment(Pos.CENTER_LEFT);
        progressRow.setPadding(new Insets(0, 0, 0, indentLevel * 15 + 20));
        
        ProgressBar miniProgress = new ProgressBar(goal.getProgress() / 100.0);
        miniProgress.setPrefWidth(100);
        miniProgress.setPrefHeight(4);
        miniProgress.setStyle("-fx-accent: " + getProgressColor(goal.getProgress()) + ";");
        
        Label progressText = new Label(String.format("%.0f%%", goal.getProgress()));
        progressText.setStyle(getMiniProgressTextStyle());
        
        // Add sub-goal count if any
        if (!goal.getSubGoals().isEmpty()) {
            Label subGoalCount = new Label("(" + goal.getSubGoals().size() + " sub-goals)");
            subGoalCount.setStyle(getSubGoalCountStyle());
            progressRow.getChildren().addAll(miniProgress, progressText, subGoalCount);
        } else {
            progressRow.getChildren().addAll(miniProgress, progressText);
        }
        
        goalContainer.getChildren().addAll(goalButton, progressRow);
        leftColumn.getChildren().add(goalContainer);
        
        // Recursively add child goals
        for (Goal childGoal : goal.getSubGoals()) {
            addGoalToColumn(childGoal, indentLevel + 1);
        }
    }
    
    /**
     * Gets appropriate hierarchy prefix based on level and goal type
     */
    private String getHierarchyPrefix(int indentLevel, Goal goal) {
        if (indentLevel == 0) {
            return "🎯 "; // Root level goals
        } else if (indentLevel == 1) {
            return "📅 "; // Year level goals
        } else if (indentLevel == 2) {
            return "📋 "; // Month/project level goals
        } else {
            return "📌 "; // Task level goals
        }
    }
    
    /**
     * Gets progress color based on completion percentage
     */
    private String getProgressColor(double progress) {
        if (progress >= 100.0) return "#27ae60"; // Green for completed
        if (progress >= 75.0) return "#3498db";  // Blue for high progress
        if (progress >= 50.0) return "#f39c12";  // Orange for medium progress
        if (progress >= 25.0) return "#e67e22";  // Dark orange for low progress
        return "#95a5a6"; // Gray for no progress
    }
    
    /**
     * Updates the right column with selected goal's details and tasks
     */
    public void updateRightColumn(Goal selectedGoal) {
        if (rightColumn == null) return;
        logger.info("Updating goal details for: " + selectedGoal.getTitle());
        
        // Store the currently selected goal
        this.currentlySelectedGoal = selectedGoal;
        
        rightColumn.getChildren().clear();
        
        // Goal header with details
        VBox goalHeader = createGoalHeader(selectedGoal);
        rightColumn.getChildren().add(goalHeader);
        
        // Tasks section
        Label tasksTitle = new Label("📝 Tasks (" + selectedGoal.getTasks().size() + ")");
        tasksTitle.setStyle(getSubSectionTitleStyle());
        rightColumn.getChildren().addAll(new Separator(), tasksTitle);
        
        // Sort tasks: status (in_progress, to_do, done), then priority, then due date
        List<Task> tasks = selectedGoal.getTasks();
        tasks.sort((task1, task2) -> {
            // 1. Status priority: IN_PROGRESS(0), TO_DO(1), DONE(2)
            int s1 = getStatusOrder(task1.getStatus());
            int s2 = getStatusOrder(task2.getStatus());
            if (s1 != s2) {
                return Integer.compare(s1, s2);
            }
            // 2. For non-completed tasks, sort by priority
            if (task1.getStatus() != Task.TaskStatus.DONE && task2.getStatus() != Task.TaskStatus.DONE) {
                int p1 = getPriorityOrder(task1.getPriority());
                int p2 = getPriorityOrder(task2.getPriority());
                if (p1 != p2) {
                    return Integer.compare(p1, p2);
                }
            }
            // 3. For same status/priority, sort by due date (earliest first)
            if (task1.getDueDate() != null && task2.getDueDate() != null) {
                return task1.getDueDate().compareTo(task2.getDueDate());
            } else if (task1.getDueDate() == null) {
                return 1;
            } else if (task2.getDueDate() == null) {
                return -1;
            }
            return 0;
        });
        
        for (Task task : tasks) {
            VBox taskCard = createTaskCard(task);
            rightColumn.getChildren().add(taskCard);
        }
        
        if (tasks.isEmpty()) {
            rightColumn.getChildren().add(createEmptyTasksMessage());
        }
        
        // Add Task button
        Button addTaskBtn = new Button("➕ Add New Task");
        addTaskBtn.setStyle(getActionButtonStyle());
        addTaskBtn.setOnAction(e -> mainView.addTask());
        rightColumn.getChildren().addAll(new Separator(), addTaskBtn);
    }
    
    /**
     * Creates a header section for the selected goal with key information and hierarchy
     */
    private VBox createGoalHeader(Goal goal) {
        VBox header = new VBox(15);
        header.setPadding(new Insets(15));
        header.setStyle(getGoalHeaderStyle());
        
        // Hierarchical path
        VBox hierarchySection = createHierarchySection(goal);
        
        // Goal name and type
        Label nameLabel = new Label(goal.getTitle());
        nameLabel.setStyle(getGoalNameStyle());
        
        Label typeLabel = new Label("Type: " + goal.getType().toString());
        typeLabel.setStyle(getGoalTypeStyle());
        
        // Progress information with visual enhancement
        VBox progressSection = createProgressSection(goal);
        
        // Dates information
        VBox datesInfo = new VBox(5);
        if (goal.getTargetDate() != null) {
            Label targetDateLabel = new Label("🎯 Target: " + goal.getTargetDate().toString());
            targetDateLabel.setStyle(getDateStyle());
            datesInfo.getChildren().add(targetDateLabel);
        }
        
        // Sub-goals and parent goal info
        VBox relationshipInfo = createRelationshipInfo(goal);
        
        // Notes if available
        if (goal.getNotes() != null && !goal.getNotes().trim().isEmpty()) {
            Label notesLabel = new Label("📝 Notes: " + goal.getNotes());
            notesLabel.setStyle(getNotesStyle());
            notesLabel.setWrapText(true);
            datesInfo.getChildren().add(notesLabel);
        }
        
        header.getChildren().addAll(hierarchySection, nameLabel, typeLabel, progressSection, relationshipInfo, datesInfo);
        return header;
    }
    
    /**
     * Creates hierarchy breadcrumb section showing parent path
     */
    private VBox createHierarchySection(Goal goal) {
        VBox hierarchySection = new VBox(8);
        
        Label hierarchyTitle = new Label("🔗 Goal Hierarchy");
        hierarchyTitle.setStyle(getSubSectionTitleStyle());
        
        // Create breadcrumb path
        HBox breadcrumb = new HBox(5);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);
        
        String[] pathParts = goal.getHierarchicalPath().split(" > ");
        for (int i = 0; i < pathParts.length; i++) {
            Label pathLabel = new Label(pathParts[i]);
            pathLabel.setStyle(i == pathParts.length - 1 ? getBreadcrumbCurrentStyle() : getBreadcrumbStyle());
            breadcrumb.getChildren().add(pathLabel);
            
            if (i < pathParts.length - 1) {
                Label separator = new Label(">");
                separator.setStyle(getBreadcrumbSeparatorStyle());
                breadcrumb.getChildren().add(separator);
            }
        }
        
        hierarchySection.getChildren().addAll(hierarchyTitle, breadcrumb);
        return hierarchySection;
    }
    
    /**
     * Creates enhanced progress section with multiple progress indicators
     */
    private VBox createProgressSection(Goal goal) {
        VBox progressSection = new VBox(8);
        
        Label progressTitle = new Label("📊 Progress Overview");
        progressTitle.setStyle(getSubSectionTitleStyle());
        
        // Overall progress
        HBox overallProgress = new HBox(10);
        overallProgress.setAlignment(Pos.CENTER_LEFT);
        
        ProgressBar progressBar = new ProgressBar(goal.getProgress() / 100.0);
        progressBar.setPrefWidth(200);
        progressBar.setStyle(getProgressBarStyle());
        
        Label progressLabel = new Label(String.format("%.1f%%", goal.getProgress()));
        progressLabel.setStyle(getProgressLabelStyle());
        
        overallProgress.getChildren().addAll(new Label("Overall:"), progressBar, progressLabel);
        
        // Task breakdown
        List<Task> tasks = goal.getTasks();
        if (!tasks.isEmpty()) {
            HBox taskBreakdown = new HBox(15);
            taskBreakdown.setAlignment(Pos.CENTER_LEFT);
            
            long completedTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.DONE).count();
            long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS).count();
            long todoTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.TO_DO).count();
            
            Label completedLabel = new Label("✅ " + completedTasks);
            completedLabel.setStyle(getTaskCountStyle("#27ae60"));
            
            Label inProgressLabel = new Label("🔄 " + inProgressTasks);
            inProgressLabel.setStyle(getTaskCountStyle("#f39c12"));
            
            Label todoLabel = new Label("⏳ " + todoTasks);
            todoLabel.setStyle(getTaskCountStyle("#95a5a6"));
            
            taskBreakdown.getChildren().addAll(
                new Label("Tasks:"), completedLabel, inProgressLabel, todoLabel
            );
            
            progressSection.getChildren().add(taskBreakdown);
        }
        
        progressSection.getChildren().addAll(progressTitle, overallProgress);
        return progressSection;
    }
    
    /**
     * Creates relationship info showing parent and child goals
     */
    private VBox createRelationshipInfo(Goal goal) {
        VBox relationshipSection = new VBox(8);
        
        // Parent goal
        if (goal.getParentGoal() != null) {
            Label parentTitle = new Label("⬆️ Parent Goal");
            parentTitle.setStyle(getSubSectionTitleStyle());
            
            Button parentButton = new Button("📁 " + goal.getParentGoal().getTitle());
            parentButton.setStyle(getRelationshipButtonStyle());
            parentButton.setOnAction(e -> updateRightColumn(goal.getParentGoal()));
            
            relationshipSection.getChildren().addAll(parentTitle, parentButton);
        }
        
        // Child goals
        List<Goal> childGoals = goal.getSubGoals();
        if (!childGoals.isEmpty()) {
            Label childTitle = new Label("⬇️ Sub Goals (" + childGoals.size() + ")");
            childTitle.setStyle(getSubSectionTitleStyle());
            
            VBox childButtons = new VBox(5);
            for (Goal childGoal : childGoals) {
                Button childButton = new Button("📂 " + childGoal.getTitle());
                childButton.setStyle(getRelationshipButtonStyle());
                childButton.setOnAction(e -> updateRightColumn(childGoal));
                childButtons.getChildren().add(childButton);
            }
            
            relationshipSection.getChildren().addAll(childTitle, childButtons);
        }
        
        return relationshipSection;
    }
    
    /**
     * Creates an interactive task card with editable status and priority
     */
    private VBox createTaskCard(Task task) {
        VBox taskCard = new VBox(12);
        taskCard.setPadding(new Insets(15));
        taskCard.setStyle(getTaskCardStyle(task.getStatus()));
        
        // Add hover effects for better interactivity
        taskCard.setOnMouseEntered(e -> taskCard.setStyle(getTaskCardHoverStyle(task.getStatus())));
        taskCard.setOnMouseExited(e -> taskCard.setStyle(getTaskCardStyle(task.getStatus())));
        
        // Task header with name, priority, and status controls
        VBox taskHeader = createTaskHeader(task);
        
        // Task description (editable)
        TextArea descriptionArea = new TextArea(task.getDescription());
        descriptionArea.setPromptText("Add task description...");
        descriptionArea.setPrefRowCount(2);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle(getTaskDescriptionStyle());
        descriptionArea.textProperty().addListener((obs, oldText, newText) -> {
            task.setDescription(newText);
            goalService.saveGoalsToFile();
        });
        
        // Task dates section
        HBox datesSection = createTaskDatesSection(task);
        
        // Task controls section
        HBox controlsSection = createTaskControlsSection(task);
        
        taskCard.getChildren().addAll(taskHeader, descriptionArea, datesSection, controlsSection);
        
        return taskCard;
    }
    
    /**
     * Creates task header with name and status indicators
     */
    private VBox createTaskHeader(Task task) {
        VBox header = new VBox(8);
        
        // Task name and completion indicator
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        
        Label taskName = new Label(task.getTitle());
        taskName.setStyle(getTaskNameStyle(task.getStatus()));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Status badge
        Label statusBadge = new Label(getStatusIcon(task.getStatus()) + " " + task.getStatus().toString());
        statusBadge.setStyle(getStatusBadgeStyle(task.getStatus()));
        
        nameRow.getChildren().addAll(taskName, spacer, statusBadge);
        
        // Priority and quick actions row
        HBox priorityRow = new HBox(10);
        priorityRow.setAlignment(Pos.CENTER_LEFT);
        
        Label priorityBadge = new Label(getPriorityIcon(task.getPriority()) + " " + task.getPriority().toString());
        priorityBadge.setStyle(getPriorityBadgeStyle(task.getPriority()));
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        // Quick status change buttons
        HBox quickActions = createQuickStatusActions(task);
        
        priorityRow.getChildren().addAll(priorityBadge, spacer2, quickActions);
        
        header.getChildren().addAll(nameRow, priorityRow);
        return header;
    }
    
    /**
     * Creates quick action buttons for status changes
     */
    private HBox createQuickStatusActions(Task task) {
        HBox actions = new HBox(5);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        // To Do button
        Button todoBtn = new Button("⏳");
        todoBtn.setStyle(getQuickActionButtonStyle(task.getStatus() == Task.TaskStatus.TO_DO));
        todoBtn.setTooltip(new Tooltip("Mark as To Do"));
        todoBtn.setOnAction(e -> updateTaskStatus(task, Task.TaskStatus.TO_DO));
        
        // In Progress button
        Button progressBtn = new Button("🔄");
        progressBtn.setStyle(getQuickActionButtonStyle(task.getStatus() == Task.TaskStatus.IN_PROGRESS));
        progressBtn.setTooltip(new Tooltip("Mark as In Progress"));
        progressBtn.setOnAction(e -> updateTaskStatus(task, Task.TaskStatus.IN_PROGRESS));
        
        // Done button
        Button doneBtn = new Button("✅");
        doneBtn.setStyle(getQuickActionButtonStyle(task.getStatus() == Task.TaskStatus.DONE));
        doneBtn.setTooltip(new Tooltip("Mark as Done"));
        doneBtn.setOnAction(e -> updateTaskStatus(task, Task.TaskStatus.DONE));
        
        actions.getChildren().addAll(todoBtn, progressBtn, doneBtn);
        return actions;
    }
    
    /**
     * Creates task dates section with start and due dates
     */
    private HBox createTaskDatesSection(Task task) {
        HBox datesContainer = new HBox(20);
        datesContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Start date
        VBox startDateBox = new VBox(3);
        Label startLabel = new Label("Start Date");
        startLabel.setStyle(getDateLabelStyle());
        
        DatePicker startDatePicker = new DatePicker(task.getStartDay());
        startDatePicker.setPromptText("Select start date");
        startDatePicker.setStyle(getDatePickerStyle());
        startDatePicker.setOnAction(e -> {
            task.setStartDay(startDatePicker.getValue());
            goalService.saveGoalsToFile();
            updateCurrentGoalDisplay();
        });
        
        startDateBox.getChildren().addAll(startLabel, startDatePicker);
        
        // Due date
        VBox dueDateBox = new VBox(3);
        Label dueLabel = new Label("Due Date");
        dueLabel.setStyle(getDateLabelStyle());
        
        DatePicker dueDatePicker = new DatePicker(task.getDueDate());
        dueDatePicker.setPromptText("Select due date");
        dueDatePicker.setStyle(getDatePickerStyle());
        dueDatePicker.setOnAction(e -> {
            task.setDueDate(dueDatePicker.getValue());
            goalService.saveGoalsToFile();
            updateCurrentGoalDisplay();
        });
        
        dueDateBox.getChildren().addAll(dueLabel, dueDatePicker);
        
        datesContainer.getChildren().addAll(startDateBox, dueDateBox);
        return datesContainer;
    }
    
    /**
     * Creates task controls section with priority selector
     */
    private HBox createTaskControlsSection(Task task) {
        HBox controlsContainer = new HBox(15);
        controlsContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Priority selector
        VBox priorityBox = new VBox(3);
        Label priorityLabel = new Label("Priority");
        priorityLabel.setStyle(getDateLabelStyle());
        
        ComboBox<Task.Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Task.Priority.values());
        priorityCombo.setValue(task.getPriority());
        priorityCombo.setStyle(getComboBoxStyle());
        priorityCombo.setOnAction(e -> {
            task.setPriority(priorityCombo.getValue());
            goalService.saveGoalsToFile();
            updateCurrentGoalDisplay();
        });
        
        priorityBox.getChildren().addAll(priorityLabel, priorityCombo);
        
        // Status selector (alternative to quick buttons)
        VBox statusBox = new VBox(3);
        Label statusLabel = new Label("Status");
        statusLabel.setStyle(getDateLabelStyle());
        
        ComboBox<Task.TaskStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(Task.TaskStatus.values());
        statusCombo.setValue(task.getStatus());
        statusCombo.setStyle(getComboBoxStyle());
        statusCombo.setOnAction(e -> updateTaskStatus(task, statusCombo.getValue()));
        
        statusBox.getChildren().addAll(statusLabel, statusCombo);
        
        controlsContainer.getChildren().addAll(priorityBox, statusBox);
        return controlsContainer;
    }
    
    /**
     * Updates task status and refreshes the UI
     */
    private void updateTaskStatus(Task task, Task.TaskStatus newStatus) {
        Task.TaskStatus oldStatus = task.getStatus();
        
        // Update task status
        task.setStatus(newStatus);
        
        // Update completion status and date
        task.setCompleted(newStatus == Task.TaskStatus.DONE);
        if (newStatus == Task.TaskStatus.DONE) {
            task.setCompletedDate(java.time.LocalDate.now());
        } else {
            task.setCompletedDate(null);
        }
        
        // Save changes
        goalService.saveGoalsToFile();
        
        // Log the status change
        logger.info("Task '" + task.getTitle() + "' status changed from " + oldStatus + " to " + newStatus);
        
        // Refresh the current goal display to show updated progress and styling
        updateCurrentGoalDisplay();
        
        // Show user feedback
        showInfoMessage("Task status updated to: " + newStatus.toString());
    }
    
    /**
     * Refreshes the current goal display (both left and right columns)
     */
    private void updateCurrentGoalDisplay() {
        updateLeftColumn(); // Refresh progress indicators in goals list
        
        // Refresh the right column with the currently selected goal
        if (currentlySelectedGoal != null) {
            updateRightColumn(currentlySelectedGoal); // Refresh task cards with updated status styling
        }
    }
    
    /**
     * Attempts to find the currently displayed goal by looking at the right column content
     * @deprecated Use currentlySelectedGoal field instead
     */
    @Deprecated
    private Goal getCurrentlyDisplayedGoal() {
        // Since we now store the current goal reference, this method is deprecated
        // but kept for backward compatibility
        if (currentlySelectedGoal != null) {
            return currentlySelectedGoal;
        }
        
        // Fallback logic if currentlySelectedGoal is somehow null
        List<Goal> allGoals = goalService.getAllGoals();
        if (!allGoals.isEmpty()) {
            // For now, return the first goal that has tasks displayed
            for (Goal goal : allGoals) {
                if (!goal.getTasks().isEmpty()) {
                    return goal;
                }
            }
            // If no goal has tasks, return the first goal
            return allGoals.get(0);
        }
        return null;
    }
    
    /**
     * Creates an empty state message when no goals exist
     */
    private VBox createEmptyGoalsMessage() {
        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(20));
        
        Label icon = new Label("🎯");
        icon.setStyle("-fx-font-size: 48px;");
        
        Label message = new Label("No goals created yet");
        message.setStyle(getEmptyStateStyle());
        
        Label suggestion = new Label("Create your first goal to get started!");
        suggestion.setStyle(getEmptyStateSuggestionStyle());
        
        Button createGoalBtn = new Button("✨ Create Goal");
        createGoalBtn.setStyle(getActionButtonStyle());
        createGoalBtn.setOnAction(e -> mainView.createGoal());
        
        emptyState.getChildren().addAll(icon, message, suggestion, createGoalBtn);
        return emptyState;
    }
    
    /**
     * Creates an empty state message when selected goal has no tasks
     */
    private VBox createEmptyTasksMessage() {
        VBox emptyState = new VBox(8);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(15));
        
        Label icon = new Label("📝");
        icon.setStyle("-fx-font-size: 32px;");
        
        Label message = new Label("No tasks for this goal");
        message.setStyle(getEmptyStateStyle());
        
        emptyState.getChildren().addAll(icon, message);
        return emptyState;
    }
    
    /**
     * Creates an empty state message when no goal is selected
     */
    private VBox createEmptyStateMessage() {
        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(20));
        
        Label icon = new Label("👈");
        icon.setStyle("-fx-font-size: 48px;");
        
        Label message = new Label("Select a goal to view details");
        message.setStyle(getEmptyStateStyle());
        
        emptyState.getChildren().addAll(icon, message);
        return emptyState;
    }
    
    /**
     * Shows an information message to the user
     */
    private void showInfoMessage(String message) {
        // For now, log the message. In future could show toast/snackbar
        logger.info("User notification: " + message);
    }
    
    // Helper methods for task/goal properties
    private int getStatusOrder(Task.TaskStatus status) {
        if (status == Task.TaskStatus.IN_PROGRESS) return 0;
        if (status == Task.TaskStatus.TO_DO) return 1;
        return 2; // DONE
    }
    
    private int getPriorityOrder(Task.Priority priority) {
        switch (priority) {
            case URGENT: return 0;
            case HIGH: return 1;
            case MEDIUM: return 2;
            case LOW: return 3;
        }
        return 4;
    }
    
    private String getPriorityIcon(Task.Priority priority) {
        switch (priority) {
            case URGENT: return "🔴";
            case HIGH: return "🟠";
            case MEDIUM: return "🟡";
            case LOW: return "🟢";
            default: return "⚪";
        }
    }
    
    private String getStatusIcon(Task.TaskStatus status) {
        switch (status) {
            case TO_DO: return "⏳";
            case IN_PROGRESS: return "🔄";
            case DONE: return "✅";
            default: return "❓";
        }
    }
    
    // Styling methods
    private void updateLayoutTheme(HBox layout) {
        String backgroundStyle = isDarkTheme ?
            "-fx-background-color: linear-gradient(to bottom right, #2c3e50, #34495e);" :
            "-fx-background-color: linear-gradient(to bottom right, #e0eafc, #cfdef3);";
        layout.setStyle(backgroundStyle);
    }
    
    private String getLeftPanelStyle() {
        return isDarkTheme ?
            "-fx-background-color: #34495e; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0.1, 0, 2);" :
            "-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.1, 0, 2);";
    }
    
    private String getRightPanelStyle() {
        return isDarkTheme ?
            "-fx-background-color: #34495e; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0.1, 0, 2);" :
            "-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.1, 0, 2);";
    }
    
    private String getSectionTitleStyle() {
        return isDarkTheme ?
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;" :
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;";
    }
    
    private String getSubSectionTitleStyle() {
        return isDarkTheme ?
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #bdc3c7;" :
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;";
    }
    
    private String getGoalButtonStyle(Goal goal) {
        String baseStyle = isDarkTheme ?
            "-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-padding: 8px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-alignment: center-left;" :
            "-fx-background-color: transparent; -fx-text-fill: #2c3e50; -fx-padding: 8px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-alignment: center-left;";
        
        // Add progress indicator color
        if (goal.getProgress() >= 100.0) {
            return baseStyle + " -fx-border-color: #27ae60; -fx-border-width: 0 0 0 3;";
        } else if (goal.getProgress() > 0) {
            return baseStyle + " -fx-border-color: #f39c12; -fx-border-width: 0 0 0 3;";
        } else {
            return baseStyle + " -fx-border-color: #95a5a6; -fx-border-width: 0 0 0 3;";
        }
    }
    
    private String getGoalButtonHoverStyle() {
        return isDarkTheme ?
            "-fx-background-color: #2c3e50; -fx-text-fill: #ecf0f1; -fx-padding: 8px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-alignment: center-left;" :
            "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-padding: 8px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-alignment: center-left;";
    }
    
    private String getActionButtonStyle() {
        return isDarkTheme ?
            "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 6px; -fx-cursor: hand;" :
            "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 6px; -fx-cursor: hand;";
    }
    
    private String getGoalHeaderStyle() {
        return isDarkTheme ?
            "-fx-background-color: #2c3e50; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0.1, 0, 1);" :
            "-fx-background-color: #f8f9fa; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.1, 0, 1);";
    }
    
    private String getGoalNameStyle() {
        return isDarkTheme ?
            "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;" :
            "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;";
    }
    
    private String getGoalTypeStyle() {
        return isDarkTheme ?
            "-fx-font-size: 14px; -fx-text-fill: #bdc3c7;" :
            "-fx-font-size: 14px; -fx-text-fill: #7f8c8d;";
    }
    
    private String getProgressBarStyle() {
        return "-fx-accent: #3498db;";
    }
    
    private String getProgressLabelStyle() {
        return isDarkTheme ?
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;" :
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;";
    }
    
    private String getDateStyle() {
        return isDarkTheme ?
            "-fx-font-size: 12px; -fx-text-fill: #95a5a6;" :
            "-fx-font-size: 12px; -fx-text-fill: #7f8c8d;";
    }
    
    private String getNotesStyle() {
        return isDarkTheme ?
            "-fx-font-size: 12px; -fx-text-fill: #bdc3c7; -fx-font-style: italic;" :
            "-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;";
    }
    
    private String getTaskCardStyle(Task.TaskStatus status) {
        String baseStyle = isDarkTheme ?
            "-fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0.2, 0, 2); -fx-border-width: 1px; -fx-border-radius: 8px;" :
            "-fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.2, 0, 2); -fx-border-width: 1px; -fx-border-radius: 8px;";
        
        switch (status) {
            case DONE:
                return baseStyle + (isDarkTheme ? 
                    " -fx-background-color: linear-gradient(to bottom, #1e7e34, #28a745); -fx-border-color: #20c997;" : 
                    " -fx-background-color: linear-gradient(to bottom, #d4edda, #c3e6cb); -fx-border-color: #28a745;");
            case IN_PROGRESS:
                return baseStyle + (isDarkTheme ? 
                    " -fx-background-color: linear-gradient(to bottom, #b8860b, #ffc107); -fx-border-color: #fd7e14;" : 
                    " -fx-background-color: linear-gradient(to bottom, #fff3cd, #ffeaa7); -fx-border-color: #ffc107;");
            default: // TO_DO
                return baseStyle + (isDarkTheme ? 
                    " -fx-background-color: linear-gradient(to bottom, #495057, #6c757d); -fx-border-color: #adb5bd;" : 
                    " -fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef); -fx-border-color: #ced4da;");
        }
    }
    
    private String getTaskCardHoverStyle(Task.TaskStatus status) {
        String baseStyle = isDarkTheme ?
            "-fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0.3, 0, 4); -fx-border-width: 2px; -fx-border-radius: 8px;" :
            "-fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.3, 0, 4); -fx-border-width: 2px; -fx-border-radius: 8px;";
        
        switch (status) {
            case DONE:
                return baseStyle + (isDarkTheme ? 
                    " -fx-background-color: linear-gradient(to bottom, #28a745, #34ce57); -fx-border-color: #20c997;" : 
                    " -fx-background-color: linear-gradient(to bottom, #c3e6cb, #b8dcc0); -fx-border-color: #20c997;");
            case IN_PROGRESS:
                return baseStyle + (isDarkTheme ? 
                    " -fx-background-color: linear-gradient(to bottom, #ffc107, #ffcd39); -fx-border-color: #fd7e14;" : 
                    " -fx-background-color: linear-gradient(to bottom, #ffeaa7, #fdd835); -fx-border-color: #ff9800;");
            default: // TO_DO
                return baseStyle + (isDarkTheme ? 
                    " -fx-background-color: linear-gradient(to bottom, #6c757d, #868e96); -fx-border-color: #868e96;" : 
                    " -fx-background-color: linear-gradient(to bottom, #e9ecef, #dee2e6); -fx-border-color: #adb5bd;");
        }
    }
    
    private String getTaskNameStyle(Task.TaskStatus status) {
        String baseStyle = isDarkTheme ?
            "-fx-font-size: 14px; -fx-font-weight: bold;" :
            "-fx-font-size: 14px; -fx-font-weight: bold;";
        
        if (status == Task.TaskStatus.DONE) {
            return baseStyle + (isDarkTheme ? 
                " -fx-text-fill: #a8e6b7; -fx-strikethrough: true;" : 
                " -fx-text-fill: #155724; -fx-strikethrough: true;");
        } else {
            return baseStyle + (isDarkTheme ? 
                " -fx-text-fill: #ecf0f1;" : 
                " -fx-text-fill: #2c3e50;");
        }
    }
    
    private String getPriorityStyle(Task.Priority priority) {
        String baseStyle = "-fx-font-size: 12px; -fx-font-weight: bold;";
        switch (priority) {
            case URGENT:
                return baseStyle + " -fx-text-fill: #e74c3c;";
            case HIGH:
                return baseStyle + " -fx-text-fill: #e67e22;";
            case MEDIUM:
                return baseStyle + " -fx-text-fill: #f1c40f;";
            case LOW:
                return baseStyle + " -fx-text-fill: #27ae60;";
            default:
                return baseStyle + (isDarkTheme ? " -fx-text-fill: #95a5a6;" : " -fx-text-fill: #7f8c8d;");
        }
    }
    
    private String getStatusStyle(Task.TaskStatus status) {
        String baseStyle = "-fx-font-size: 12px; -fx-font-weight: bold;";
        switch (status) {
            case DONE:
                return baseStyle + " -fx-text-fill: #27ae60;";
            case IN_PROGRESS:
                return baseStyle + " -fx-text-fill: #f39c12;";
            default: // TO_DO
                return baseStyle + (isDarkTheme ? " -fx-text-fill: #95a5a6;" : " -fx-text-fill: #7f8c8d;");
        }
    }
    
    private String getTaskDateStyle() {
        return isDarkTheme ?
            "-fx-font-size: 11px; -fx-text-fill: #95a5a6;" :
            "-fx-font-size: 11px; -fx-text-fill: #7f8c8d;";
    }
    
    private String getEmptyStateStyle() {
        return isDarkTheme ?
            "-fx-font-size: 16px; -fx-text-fill: #bdc3c7;" :
            "-fx-font-size: 16px; -fx-text-fill: #7f8c8d;";
    }
    
    private String getEmptyStateSuggestionStyle() {
        return isDarkTheme ?
            "-fx-font-size: 12px; -fx-text-fill: #95a5a6;" :
            "-fx-font-size: 12px; -fx-text-fill: #95a5a6;";
    }
    
    // New styling methods for enhanced UI
    private String getBreadcrumbStyle() {
        return isDarkTheme ?
            "-fx-font-size: 12px; -fx-text-fill: #bdc3c7; -fx-padding: 3px 6px; -fx-background-color: transparent;" :
            "-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 3px 6px; -fx-background-color: transparent;";
    }
    
    private String getBreadcrumbCurrentStyle() {
        return isDarkTheme ?
            "-fx-font-size: 12px; -fx-text-fill: #3498db; -fx-font-weight: bold; -fx-padding: 3px 6px; -fx-background-color: rgba(52, 152, 219, 0.2); -fx-background-radius: 3px;" :
            "-fx-font-size: 12px; -fx-text-fill: #2980b9; -fx-font-weight: bold; -fx-padding: 3px 6px; -fx-background-color: rgba(52, 152, 219, 0.1); -fx-background-radius: 3px;";
    }
    
    private String getBreadcrumbSeparatorStyle() {
        return isDarkTheme ?
            "-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 3px;" :
            "-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-padding: 3px;";
    }
    
    private String getTaskCountStyle(String color) {
        return "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + "; " +
               "-fx-padding: 2px 6px; -fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8px;";
    }
    
    private String getRelationshipButtonStyle() {
        return isDarkTheme ?
            "-fx-background-color: #2c3e50; -fx-text-fill: #ecf0f1; -fx-font-size: 12px; -fx-padding: 5px 10px; " +
            "-fx-background-radius: 4px; -fx-cursor: hand; -fx-border-color: #34495e; -fx-border-width: 1px;" :
            "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-padding: 5px 10px; " +
            "-fx-background-radius: 4px; -fx-cursor: hand; -fx-border-color: #bdc3c7; -fx-border-width: 1px;";
    }
    
    private String getStatusBadgeStyle(Task.TaskStatus status) {
        String baseStyle = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 10px;";
        switch (status) {
            case DONE:
                return baseStyle + " -fx-background-color: #27ae60; -fx-text-fill: white;";
            case IN_PROGRESS:
                return baseStyle + " -fx-background-color: #f39c12; -fx-text-fill: white;";
            default: // TO_DO
                return baseStyle + " -fx-background-color: #95a5a6; -fx-text-fill: white;";
        }
    }
    
    private String getPriorityBadgeStyle(Task.Priority priority) {
        String baseStyle = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 10px;";
        switch (priority) {
            case URGENT:
                return baseStyle + " -fx-background-color: #e74c3c; -fx-text-fill: white;";
            case HIGH:
                return baseStyle + " -fx-background-color: #e67e22; -fx-text-fill: white;";
            case MEDIUM:
                return baseStyle + " -fx-background-color: #f1c40f; -fx-text-fill: black;";
            case LOW:
                return baseStyle + " -fx-background-color: #27ae60; -fx-text-fill: white;";
            default:
                return baseStyle + " -fx-background-color: #95a5a6; -fx-text-fill: white;";
        }
    }
    
    private String getQuickActionButtonStyle(boolean isActive) {
        String baseStyle = "-fx-font-size: 14px; -fx-padding: 4px 8px; -fx-background-radius: 15px; -fx-cursor: hand; -fx-border-width: 0;";
        if (isActive) {
            return baseStyle + (isDarkTheme ?
                " -fx-background-color: #3498db; -fx-text-fill: white;" :
                " -fx-background-color: #3498db; -fx-text-fill: white;");
        } else {
            return baseStyle + (isDarkTheme ?
                " -fx-background-color: #4a5568; -fx-text-fill: #bdc3c7;" :
                " -fx-background-color: #ecf0f1; -fx-text-fill: #7f8c8d;");
        }
    }
    
    private String getTaskDescriptionStyle() {
        return isDarkTheme ?
            "-fx-control-inner-background: #4a5568; -fx-text-fill: #ecf0f1; -fx-font-size: 12px; " +
            "-fx-background-color: #4a5568; -fx-border-color: #5a6c7d; -fx-border-radius: 4px;" :
            "-fx-control-inner-background: #f8f9fa; -fx-text-fill: #2c3e50; -fx-font-size: 12px; " +
            "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 4px;";
    }
    
    private String getDateLabelStyle() {
        return isDarkTheme ?
            "-fx-font-size: 11px; -fx-text-fill: #bdc3c7; -fx-font-weight: bold;" :
            "-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;";
    }
    
    private String getDatePickerStyle() {
        return isDarkTheme ?
            "-fx-background-color: #4a5568; -fx-text-fill: #ecf0f1; -fx-font-size: 11px;" :
            "-fx-background-color: white; -fx-text-fill: #2c3e50; -fx-font-size: 11px;";
    }
    
    private String getComboBoxStyle() {
        return isDarkTheme ?
            "-fx-background-color: #4a5568; -fx-text-fill: #ecf0f1; -fx-font-size: 11px;" :
            "-fx-background-color: white; -fx-text-fill: #2c3e50; -fx-font-size: 11px;";
    }
    
    // Additional styling methods for enhanced left panel
    private String getMiniProgressTextStyle() {
        return isDarkTheme ?
            "-fx-font-size: 9px; -fx-text-fill: #bdc3c7;" :
            "-fx-font-size: 9px; -fx-text-fill: #7f8c8d;";
    }
    
    private String getSubGoalCountStyle() {
        return isDarkTheme ?
            "-fx-font-size: 9px; -fx-text-fill: #95a5a6; -fx-font-style: italic;" :
            "-fx-font-size: 9px; -fx-text-fill: #95a5a6; -fx-font-style: italic;";
    }
    
    /**
     * Toggle the visibility of the hierarchy panel (left panel)
     */
    private void toggleHierarchyVisibility(Button toggleButton) {
        hierarchyVisible = !hierarchyVisible;
        
        // Update button text and styling
        toggleButton.setText(hierarchyVisible ? "👁️ Hide Hierarchy" : "👁️ Show Hierarchy");
        toggleButton.setStyle(getToggleButtonStyle());
        
        // Toggle the visibility of the left scroll pane
        if (leftScrollPane != null) {
            leftScrollPane.setVisible(hierarchyVisible);
            leftScrollPane.setManaged(hierarchyVisible);
        }
        
        logger.info("Hierarchy panel " + (hierarchyVisible ? "shown" : "hidden"));
    }
    
    /**
     * Get styling for the toggle hierarchy button
     */
    private String getToggleButtonStyle() {
        return isDarkTheme ?
            "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-font-size: 12px; -fx-padding: 8px 12px; " +
            "-fx-background-radius: 6px; -fx-cursor: hand; -fx-border-color: #2c3e50; -fx-border-width: 1px; " +
            "-fx-border-radius: 6px;" :
            "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-padding: 8px 12px; " +
            "-fx-background-radius: 6px; -fx-cursor: hand; -fx-border-color: #bdc3c7; -fx-border-width: 1px; " +
            "-fx-border-radius: 6px;";
    }
}
