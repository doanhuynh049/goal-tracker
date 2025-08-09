import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.StringConverter;
import model.*;
import service.GoalService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Add/Edit My Goals view providing comprehensive goal and task management operations.
 * This panel offers full CRUD operations for goals, tasks, and their hierarchical relationships.
 */
public class AddEditMyGoals extends VBox {
    private final GoalService goalService;
    private boolean isDarkMode = false;
    
    // UI Components for filtering and sorting
    private ComboBox<String> viewModeComboBox;
    private ComboBox<GoalType> goalTypeFilter;
    private ComboBox<Task.TaskStatus> taskStatusFilter;
    private ComboBox<Task.Priority> taskPriorityFilter;
    private TextField searchField;
    private ComboBox<String> sortByComboBox;
    
    // Content areas
    private VBox goalsContainer;
    private VBox tasksContainer;
    private ScrollPane mainScrollPane;
    private VBox mainContent;
    
    // Current filters
    private String currentViewMode = "All";
    private GoalType currentGoalTypeFilter = null;
    private Task.TaskStatus currentTaskStatusFilter = null;
    private Task.Priority currentTaskPriorityFilter = null;
    private String currentSearchText = "";
    private String currentSortBy = "Date Created";

    public AddEditMyGoals(GoalService goalService) {
        this.goalService = goalService;
        initializeView();
        setupEventHandlers();
        refreshContent();
    }

    private void initializeView() {
        this.setSpacing(20);
        this.setPadding(new Insets(20));
        this.setPrefWidth(450);
        this.setMinWidth(400);
        updateLayoutTheme();
        
        // Header with enhanced styling
        Label titleLabel = new Label("🎯 Add/Edit My Goals");
        titleLabel.setStyle(getDashboardTitleStyle());
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);
        
        // Filter and control section with enhanced styling
        VBox filterSection = createFilterSection();
        
        // Create scrollable content with enhanced styling
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        mainContent = new VBox(25);
        mainContent.setPadding(new Insets(10));
        
        // Goals and tasks containers
        goalsContainer = new VBox(15);
        tasksContainer = new VBox(15);
        
        mainContent.getChildren().addAll(goalsContainer, tasksContainer);
        scrollPane.setContent(mainContent);
        
        this.getChildren().addAll(titleLabel, filterSection, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        applyTheme();
    }

    private VBox createFilterSection() {
        VBox filterSection = new VBox(12);
        filterSection.setPadding(new Insets(20));
        filterSection.setStyle(getSectionCardStyle());
        
        // View mode selector with enhanced styling
        HBox viewModeBox = new HBox(12);
        viewModeBox.setAlignment(Pos.CENTER_LEFT);
        Label viewModeLabel = new Label("📊 View Mode:");
        viewModeLabel.setStyle(getLabelStyle());
        viewModeComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "All", "Goals Only", "Tasks Only", "Hierarchy View"
        ));
        viewModeComboBox.setValue("All");
        viewModeComboBox.setStyle(getComboBoxStyle());
        viewModeComboBox.setPrefWidth(150);
        viewModeBox.getChildren().addAll(viewModeLabel, viewModeComboBox);
        
        // Search field with modern design
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        Label searchLabel = new Label("🔍 Search:");
        searchLabel.setStyle(getLabelStyle());
        searchField = new TextField();
        searchField.setPromptText("Search goals and tasks...");
        searchField.setPrefWidth(220);
        searchField.setStyle(getTextFieldStyle());
        Button clearSearchBtn = new Button("Clear");
        clearSearchBtn.setStyle(getSecondaryButtonStyle());
        addButtonHoverEffect(clearSearchBtn, getSecondaryButtonStyle(), getSecondaryButtonHoverStyle());
        searchBox.getChildren().addAll(searchLabel, searchField, clearSearchBtn);
        
        // Filter row 1: Goal Type and Task Status
        HBox filterRow1 = new HBox(15);
        filterRow1.setAlignment(Pos.CENTER_LEFT);
        
        Label goalTypeLabel = new Label("🎯 Goal Type:");
        goalTypeLabel.setStyle(getSmallLabelStyle());
        goalTypeFilter = new ComboBox<>();
        goalTypeFilter.getItems().add(null);
        goalTypeFilter.getItems().addAll(GoalType.values());
        goalTypeFilter.setValue(null);
        goalTypeFilter.setPrefWidth(120);
        goalTypeFilter.setStyle(getSmallComboBoxStyle());
        goalTypeFilter.setConverter(new StringConverter<GoalType>() {
            @Override
            public String toString(GoalType type) {
                return type == null ? "All" : type.toString();
            }
            @Override
            public GoalType fromString(String string) {
                return "All".equals(string) ? null : GoalType.valueOf(string);
            }
        });
        
        Label taskStatusLabel = new Label("📋 Task Status:");
        taskStatusLabel.setStyle(getSmallLabelStyle());
        taskStatusFilter = new ComboBox<>();
        taskStatusFilter.getItems().add(null);
        taskStatusFilter.getItems().addAll(Task.TaskStatus.values());
        taskStatusFilter.setValue(null);
        taskStatusFilter.setPrefWidth(120);
        taskStatusFilter.setStyle(getSmallComboBoxStyle());
        taskStatusFilter.setConverter(new StringConverter<Task.TaskStatus>() {
            @Override
            public String toString(Task.TaskStatus status) {
                return status == null ? "All" : status.toString();
            }
            @Override
            public Task.TaskStatus fromString(String string) {
                return "All".equals(string) ? null : Task.TaskStatus.valueOf(string);
            }
        });
        
        filterRow1.getChildren().addAll(goalTypeLabel, goalTypeFilter, taskStatusLabel, taskStatusFilter);
        
        // Filter row 2: Task Priority and Sort
        HBox filterRow2 = new HBox(15);
        filterRow2.setAlignment(Pos.CENTER_LEFT);
        
        Label taskPriorityLabel = new Label("⚡ Priority:");
        taskPriorityLabel.setStyle(getSmallLabelStyle());
        taskPriorityFilter = new ComboBox<>();
        taskPriorityFilter.getItems().add(null);
        taskPriorityFilter.getItems().addAll(Task.Priority.values());
        taskPriorityFilter.setValue(null);
        taskPriorityFilter.setPrefWidth(120);
        taskPriorityFilter.setStyle(getSmallComboBoxStyle());
        taskPriorityFilter.setConverter(new StringConverter<Task.Priority>() {
            @Override
            public String toString(Task.Priority priority) {
                return priority == null ? "All" : priority.toString();
            }
            @Override
            public Task.Priority fromString(String string) {
                return "All".equals(string) ? null : Task.Priority.valueOf(string);
            }
        });
        
        Label sortLabel = new Label("📊 Sort By:");
        sortLabel.setStyle(getSmallLabelStyle());
        sortByComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "Date Created", "Date Modified", "Due Date", "Priority", "Status", "Title"
        ));
        sortByComboBox.setValue("Date Created");
        sortByComboBox.setPrefWidth(130);
        sortByComboBox.setStyle(getSmallComboBoxStyle());
        
        filterRow2.getChildren().addAll(taskPriorityLabel, taskPriorityFilter, sortLabel, sortByComboBox);
        
        // Action buttons with modern styling
        HBox actionButtons = new HBox(12);
        actionButtons.setAlignment(Pos.CENTER_LEFT);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));
        
        Button addGoalBtn = new Button("➕ Add Goal");
        Button addTaskBtn = new Button("➕ Add Task");
        Button refreshBtn = new Button("🔄 Refresh");
        
        addGoalBtn.setStyle(getPrimaryButtonStyle());
        addTaskBtn.setStyle(getPrimaryButtonStyle());
        refreshBtn.setStyle(getSecondaryButtonStyle());
        
        // Add hover effects
        addButtonHoverEffect(addGoalBtn, getPrimaryButtonStyle(), getPrimaryButtonHoverStyle());
        addButtonHoverEffect(addTaskBtn, getPrimaryButtonStyle(), getPrimaryButtonHoverStyle());
        addButtonHoverEffect(refreshBtn, getSecondaryButtonStyle(), getSecondaryButtonHoverStyle());
        
        actionButtons.getChildren().addAll(addGoalBtn, addTaskBtn, refreshBtn);
        
        filterSection.getChildren().addAll(viewModeBox, searchBox, filterRow1, filterRow2, actionButtons);
        
        return filterSection;
    }
    
    // Helper method for button hover effects
    private void addButtonHoverEffect(Button button, String normalStyle, String hoverStyle) {
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }
    
    // Style methods similar to DashboardRightView
    private String getSectionCardStyle() {
        return isDarkMode ?
            "-fx-background-color: #34495e; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0.1, 0, 2);" :
            "-fx-background-color: white; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(44,62,80,0.08), 6, 0.1, 0, 2);";
    }
    
    private String getLabelStyle() {
        return isDarkMode ?
            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #ecf0f1;" :
            "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #495057;";
    }
    
    private String getSmallLabelStyle() {
        return isDarkMode ?
            "-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #bdc3c7;" :
            "-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #495057;";
    }
    
    private String getComboBoxStyle() {
        return isDarkMode ?
            "-fx-font-size: 13px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: #2c3e50; -fx-border-color: #4a4a4a;" :
            "-fx-font-size: 13px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: #f8f9fa; -fx-border-color: #ced4da;";
    }
    
    private String getSmallComboBoxStyle() {
        return isDarkMode ?
            "-fx-font-size: 12px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-background-color: #2c3e50; -fx-border-color: #4a4a4a;" :
            "-fx-font-size: 12px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-background-color: #f8f9fa; -fx-border-color: #ced4da;";
    }
    
    private String getTextFieldStyle() {
        return isDarkMode ?
            "-fx-font-size: 13px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: #2c3e50; -fx-border-color: #4a4a4a; -fx-padding: 8; -fx-text-fill: #ecf0f1;" :
            "-fx-font-size: 13px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-background-color: #f8f9fa; -fx-border-color: #ced4da; -fx-padding: 8;";
    }
    
    private String getPrimaryButtonStyle() {
        return isDarkMode ?
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.3), 4, 0, 0, 2);" :
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to bottom, #007bff, #0056b3); -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,123,255,0.3), 4, 0, 0, 2);";
    }
    
    private String getPrimaryButtonHoverStyle() {
        return isDarkMode ?
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to bottom, #2980b9, #21618c); -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(52,152,219,0.5), 6, 0, 0, 3);" :
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-color: linear-gradient(to bottom, #0056b3, #004085); -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,123,255,0.5), 6, 0, 0, 3);";
    }
    
    private String getSecondaryButtonStyle() {
        return isDarkMode ?
            "-fx-font-size: 12px; -fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;" :
            "-fx-font-size: 12px; -fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;";
    }
    
    private String getSecondaryButtonHoverStyle() {
        return isDarkMode ?
            "-fx-font-size: 12px; -fx-background-color: #4a5f7a; -fx-text-fill: #ecf0f1; -fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;" :
            "-fx-font-size: 12px; -fx-background-color: #5a6268; -fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;";
    }
    
    // Styling methods similar to DashboardRightView
    private String getDashboardTitleStyle() {
        return isDarkMode ?
            "-fx-font-size: 32px; -fx-text-fill: #ecf0f1; -fx-font-weight: bold; -fx-padding: 0 0 20 0;" :
            "-fx-font-size: 32px; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-padding: 0 0 20 0;";
    }
    
    private void updateLayoutTheme() {
        String backgroundStyle = isDarkMode ?
            "-fx-background-color: linear-gradient(to bottom right, #2c3e50, #34495e);" :
            "-fx-background-color: linear-gradient(to bottom right, #e0eafc, #cfdef3);";
        this.setStyle(backgroundStyle);
    }

    private void setupEventHandlers() {
        // Filter and search event handlers
        viewModeComboBox.setOnAction(e -> {
            currentViewMode = viewModeComboBox.getValue();
            refreshContent();
        });
        
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            currentSearchText = newText != null ? newText.toLowerCase() : "";
            refreshContent();
        });
        
        goalTypeFilter.setOnAction(e -> {
            currentGoalTypeFilter = goalTypeFilter.getValue();
            refreshContent();
        });
        
        taskStatusFilter.setOnAction(e -> {
            currentTaskStatusFilter = taskStatusFilter.getValue();
            refreshContent();
        });
        
        taskPriorityFilter.setOnAction(e -> {
            currentTaskPriorityFilter = taskPriorityFilter.getValue();
            refreshContent();
        });
        
        sortByComboBox.setOnAction(e -> {
            currentSortBy = sortByComboBox.getValue();
            refreshContent();
        });
        
        // Action button handlers - find buttons in the enhanced filter section
        VBox filterSection = (VBox) getChildren().get(1);
        HBox actionButtons = (HBox) filterSection.getChildren().get(4);
        Button addGoalBtn = (Button) actionButtons.getChildren().get(0);
        Button addTaskBtn = (Button) actionButtons.getChildren().get(1);
        Button refreshBtn = (Button) actionButtons.getChildren().get(2);
        
        // Clear search button
        HBox searchBox = (HBox) filterSection.getChildren().get(1);
        Button clearSearchBtn = (Button) searchBox.getChildren().get(2);
        
        addGoalBtn.setOnAction(e -> showAddGoalDialog());
        addTaskBtn.setOnAction(e -> showAddTaskDialog());
        refreshBtn.setOnAction(e -> refreshContent());
        clearSearchBtn.setOnAction(e -> {
            searchField.clear();
            currentSearchText = "";
            refreshContent();
        });
    }

    private void refreshContent() {
        goalsContainer.getChildren().clear();
        tasksContainer.getChildren().clear();
        
        if (!"Tasks Only".equals(currentViewMode)) {
            populateGoals();
        }
        
        if (!"Goals Only".equals(currentViewMode)) {
            populateTasks();
        }
        
        // Update container visibility
        if ("Hierarchy View".equals(currentViewMode)) {
            populateHierarchyView();
        }
    }

    private void populateGoals() {
        Label goalsHeader = new Label("🎯 Goals");
        goalsHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; " +
                           "-fx-text-fill: linear-gradient(to right, #2c3e50, #3498db); " +
                           "-fx-padding: 10 0; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        goalsContainer.getChildren().add(goalsHeader);
        
        List<Goal> filteredGoals = getFilteredGoals();
        
        if (filteredGoals.isEmpty()) {
            Label emptyLabel = new Label("🔍 No goals match the current filters");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; " +
                              "-fx-padding: 30; -fx-alignment: center; " +
                              "-fx-background-color: rgba(248,249,250,0.8); " +
                              "-fx-background-radius: 10; -fx-border-color: #dee2e6; " +
                              "-fx-border-width: 1; -fx-border-radius: 10;");
            goalsContainer.getChildren().add(emptyLabel);
        } else {
            for (Goal goal : filteredGoals) {
                VBox goalCard = createGoalCard(goal);
                goalsContainer.getChildren().add(goalCard);
            }
        }
    }

    private void populateTasks() {
        Label tasksHeader = new Label("📝 Tasks");
        tasksHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; " +
                           "-fx-text-fill: linear-gradient(to right, #e74c3c, #c0392b); " +
                           "-fx-padding: 10 0; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        tasksContainer.getChildren().add(tasksHeader);
        
        List<Task> filteredTasks = getFilteredTasks();
        
        if (filteredTasks.isEmpty()) {
            Label emptyLabel = new Label("📋 No tasks match the current filters");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; " +
                              "-fx-padding: 30; -fx-alignment: center; " +
                              "-fx-background-color: rgba(248,249,250,0.8); " +
                              "-fx-background-radius: 10; -fx-border-color: #dee2e6; " +
                              "-fx-border-width: 1; -fx-border-radius: 10;");
            tasksContainer.getChildren().add(emptyLabel);
        } else {
            for (Task task : filteredTasks) {
                VBox taskCard = createTaskCard(task);
                tasksContainer.getChildren().add(taskCard);
            }
        }
    }

    private void populateHierarchyView() {
        goalsContainer.getChildren().clear();
        tasksContainer.getChildren().clear();
        
        Label hierarchyHeader = new Label("🌳 Hierarchy View");
        hierarchyHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; " +
                                "-fx-text-fill: linear-gradient(to right, #27ae60, #2ecc71); " +
                                "-fx-padding: 10 0; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        goalsContainer.getChildren().add(hierarchyHeader);
        
        // Get all goals and organize by hierarchy
        List<Goal> allGoals = goalService.getAllGoals();
        
        // Start with long-term goals (top level)
        List<Goal> longTermGoals = allGoals.stream()
            .filter(g -> g.getType() == GoalType.LONG_TERM)
            .collect(Collectors.toList());
        
        for (Goal longTermGoal : longTermGoals) {
            VBox hierarchyTree = createHierarchyTree(longTermGoal, 0);
            goalsContainer.getChildren().add(hierarchyTree);
        }
    }

    private VBox createHierarchyTree(Goal goal, int level) {
        VBox treeNode = new VBox(5);
        treeNode.setPadding(new Insets(0, 0, 0, level * 20));
        
        // Goal card
        VBox goalCard = createGoalCard(goal);
        goalCard.getStyleClass().add("hierarchy-level-" + level);
        treeNode.getChildren().add(goalCard);
        
        // Child goals
        List<Goal> childGoals = goal.getSubGoals();
        for (Goal child : childGoals) {
            VBox childTree = createHierarchyTree(child, level + 1);
            treeNode.getChildren().add(childTree);
        }
        
        // Associated tasks
        List<Task> goalTasks = goal.getTasks();
        goalTasks = filterTaskList(goalTasks);
        
        for (Task task : goalTasks) {
            VBox taskCard = createTaskCard(task);
            taskCard.setPadding(new Insets(0, 0, 0, (level + 1) * 20));
            taskCard.getStyleClass().add("hierarchy-task");
            treeNode.getChildren().add(taskCard);
        }
        
        return treeNode;
    }

    private VBox createGoalCard(Goal goal) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8f9fa); " +
                     "-fx-background-radius: 15; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4); " +
                     "-fx-border-color: linear-gradient(to right, #3498db, #2ecc71); " +
                     "-fx-border-width: 2; -fx-border-radius: 15;");
        
        // Header with title and actions
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(goal.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                          "-fx-text-fill: #2c3e50; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(200);
        titleLabel.setWrapText(true);
        
        Label typeLabel = new Label(goal.getType().toString());
        typeLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; " +
                         "-fx-background-color: linear-gradient(to right, #3498db, #2980b9); " +
                         "-fx-text-fill: white; -fx-padding: 4 8; " +
                         "-fx-background-radius: 12; -fx-border-radius: 12;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button editBtn = new Button("✏️");
        Button deleteBtn = new Button("🗑️");
        
        String iconButtonStyle = "-fx-font-size: 14px; -fx-background-radius: 20; " +
                                "-fx-min-width: 30; -fx-min-height: 30; " +
                                "-fx-max-width: 30; -fx-max-height: 30; " +
                                "-fx-cursor: hand; -fx-border-radius: 20;";
        
        editBtn.setStyle(iconButtonStyle + "-fx-background-color: linear-gradient(to bottom, #f39c12, #e67e22); " +
                        "-fx-effect: dropshadow(gaussian, rgba(243,156,18,0.4), 4, 0, 0, 2);");
        deleteBtn.setStyle(iconButtonStyle + "-fx-background-color: linear-gradient(to bottom, #e74c3c, #c0392b); " +
                          "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.4), 4, 0, 0, 2);");
        
        // Hover effects for buttons
        editBtn.setOnMouseEntered(e -> editBtn.setStyle(iconButtonStyle + 
                                                       "-fx-background-color: linear-gradient(to bottom, #e67e22, #d35400); " +
                                                       "-fx-effect: dropshadow(gaussian, rgba(243,156,18,0.6), 6, 0, 0, 3);"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(iconButtonStyle + 
                                                      "-fx-background-color: linear-gradient(to bottom, #f39c12, #e67e22); " +
                                                      "-fx-effect: dropshadow(gaussian, rgba(243,156,18,0.4), 4, 0, 0, 2);"));
        
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(iconButtonStyle + 
                                                           "-fx-background-color: linear-gradient(to bottom, #c0392b, #a93226); " +
                                                           "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.6), 6, 0, 0, 3);"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(iconButtonStyle + 
                                                          "-fx-background-color: linear-gradient(to bottom, #e74c3c, #c0392b); " +
                                                          "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.4), 4, 0, 0, 2);"));
        
        header.getChildren().addAll(titleLabel, typeLabel, spacer, editBtn, deleteBtn);
        
        // Description
        if (goal.getDescription() != null && !goal.getDescription().isEmpty()) {
            Label descLabel = new Label(goal.getDescription());
            descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #495057; " +
                              "-fx-wrap-text: true; -fx-padding: 8 0;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(350);
            card.getChildren().add(descLabel);
        }
        
        // Dates and status with enhanced styling
        HBox infoRow = new HBox(20);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.setPadding(new Insets(8, 0, 0, 0));
        
        if (goal.getTargetDate() != null) {
            Label targetLabel = new Label("📅 " + goal.getTargetDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            targetLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; " +
                               "-fx-background-color: rgba(108,117,125,0.1); " +
                               "-fx-padding: 4 8; -fx-background-radius: 8;");
            infoRow.getChildren().add(targetLabel);
        }
        
        Label statusLabel = new Label(goal.isCompleted() ? "✅ Completed" : "🔄 In Progress");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; " +
                           (goal.isCompleted() ? 
                            "-fx-text-fill: #28a745; -fx-background-color: rgba(40,167,69,0.15);" :
                            "-fx-text-fill: #007bff; -fx-background-color: rgba(0,123,255,0.15);") +
                           " -fx-padding: 4 8; -fx-background-radius: 8;");
        infoRow.getChildren().add(statusLabel);
        
        card.getChildren().addAll(header, infoRow);
        
        // Hierarchy info with enhanced styling
        if (goal.getParentGoal() != null) {
            Goal parentGoal = goal.getParentGoal();
            Label parentLabel = new Label("👆 Parent: " + parentGoal.getTitle());
            parentLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d; " +
                               "-fx-background-color: rgba(248,249,250,0.8); " +
                               "-fx-padding: 6 10; -fx-background-radius: 6; " +
                               "-fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 6;");
            card.getChildren().add(parentLabel);
        }
        
        // Progress indicator with enhanced styling
        double progress = goal.getProgress() / 100.0;
        
        VBox progressBox = new VBox(8);
        progressBox.setPadding(new Insets(10, 0, 0, 0));
        
        HBox progressHeader = new HBox(10);
        progressHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label progressLabel = new Label(String.format("📊 Progress: %.1f%%", progress * 100));
        progressLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #495057;");
        
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setPrefWidth(280);
        progressBar.setPrefHeight(8);
        progressBar.setStyle("-fx-accent: linear-gradient(to right, #28a745, #20c997); " +
                           "-fx-background-radius: 4; -fx-background-insets: 0;");
        
        progressHeader.getChildren().add(progressLabel);
        progressBox.getChildren().addAll(progressHeader, progressBar);
        card.getChildren().add(progressBox);
        
        // Event handlers
        editBtn.setOnAction(e -> showEditGoalDialog(goal));
        deleteBtn.setOnAction(e -> showDeleteGoalConfirmation(goal));
        
        return card;
    }

    private VBox createTaskCard(Task task) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8f9fa); " +
                     "-fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 3); " +
                     "-fx-border-color: linear-gradient(to right, #e74c3c, #f39c12); " +
                     "-fx-border-width: 2; -fx-border-radius: 12;");
        
        // Header with title and actions
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; " +
                          "-fx-text-fill: #2c3e50; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(180);
        titleLabel.setWrapText(true);
        
        Label priorityLabel = new Label(getPriorityIcon(task.getPriority()) + " " + task.getPriority());
        String priorityColor = getPriorityColor(task.getPriority());
        priorityLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; " +
                             "-fx-background-color: " + priorityColor + "; " +
                             "-fx-text-fill: white; -fx-padding: 3 6; " +
                             "-fx-background-radius: 10; -fx-border-radius: 10;");
        
        Label statusLabel = new Label(getStatusIcon(task.getStatus()) + " " + task.getStatus());
        String statusColor = getStatusColor(task.getStatus());
        statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; " +
                           "-fx-background-color: " + statusColor + "; " +
                           "-fx-text-fill: white; -fx-padding: 3 6; " +
                           "-fx-background-radius: 10; -fx-border-radius: 10;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button editBtn = new Button("✏️");
        Button deleteBtn = new Button("🗑️");
        
        String iconButtonStyle = "-fx-font-size: 12px; -fx-background-radius: 18; " +
                                "-fx-min-width: 26; -fx-min-height: 26; " +
                                "-fx-max-width: 26; -fx-max-height: 26; " +
                                "-fx-cursor: hand; -fx-border-radius: 18;";
        
        editBtn.setStyle(iconButtonStyle + "-fx-background-color: linear-gradient(to bottom, #f39c12, #e67e22); " +
                        "-fx-effect: dropshadow(gaussian, rgba(243,156,18,0.4), 3, 0, 0, 1);");
        deleteBtn.setStyle(iconButtonStyle + "-fx-background-color: linear-gradient(to bottom, #e74c3c, #c0392b); " +
                          "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.4), 3, 0, 0, 1);");
        
        header.getChildren().addAll(titleLabel, priorityLabel, statusLabel, spacer, editBtn, deleteBtn);
        
        // Description
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            Label descLabel = new Label(task.getDescription());
            descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057; " +
                              "-fx-wrap-text: true; -fx-padding: 6 0;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(320);
            card.getChildren().add(descLabel);
        }
        
        // Dates with enhanced styling
        HBox dateRow = new HBox(15);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        dateRow.setPadding(new Insets(6, 0, 0, 0));
        
        if (task.getDueDate() != null) {
            Label dueLabel = new Label("📅 Due: " + task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            
            // Highlight overdue tasks
            if (task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != Task.TaskStatus.DONE) {
                dueLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; " +
                                "-fx-text-fill: #dc3545; -fx-background-color: rgba(220,53,69,0.15); " +
                                "-fx-padding: 4 8; -fx-background-radius: 8; " +
                                "-fx-border-color: #dc3545; -fx-border-width: 1; -fx-border-radius: 8;");
            } else {
                dueLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d; " +
                                "-fx-background-color: rgba(108,117,125,0.1); " +
                                "-fx-padding: 4 8; -fx-background-radius: 8;");
            }
            
            dateRow.getChildren().add(dueLabel);
        }
        
        card.getChildren().addAll(header, dateRow);
        
        // Event handlers
        editBtn.setOnAction(e -> showEditTaskDialog(task));
        deleteBtn.setOnAction(e -> showDeleteTaskConfirmation(task));
        
        return card;
    }

    private List<Goal> getFilteredGoals() {
        List<Goal> goals = goalService.getAllGoals();
        return goals.stream()
            .filter(this::matchesGoalFilters)
            .sorted(this::compareGoals)
            .collect(Collectors.toList());
    }

    private List<Task> getFilteredTasks() {
        List<Task> tasks = new ArrayList<>();
        for (Goal goal : goalService.getAllGoals()) {
            tasks.addAll(goal.getAllTasks());
        }
        return filterTaskList(tasks);
    }

    private List<Task> filterTaskList(List<Task> tasks) {
        return tasks.stream()
            .filter(this::matchesTaskFilters)
            .sorted(this::compareTasks)
            .collect(Collectors.toList());
    }

    private boolean matchesGoalFilters(Goal goal) {
        // Type filter
        if (currentGoalTypeFilter != null && goal.getType() != currentGoalTypeFilter) {
            return false;
        }
        
        // Search filter
        if (!currentSearchText.isEmpty()) {
            String searchText = currentSearchText.toLowerCase();
            if (!goal.getTitle().toLowerCase().contains(searchText) &&
                (goal.getDescription() == null || !goal.getDescription().toLowerCase().contains(searchText))) {
                return false;
            }
        }
        
        return true;
    }

    private boolean matchesTaskFilters(Task task) {
        // Status filter
        if (currentTaskStatusFilter != null && task.getStatus() != currentTaskStatusFilter) {
            return false;
        }
        
        // Priority filter
        if (currentTaskPriorityFilter != null && task.getPriority() != currentTaskPriorityFilter) {
            return false;
        }
        
        // Search filter
        if (!currentSearchText.isEmpty()) {
            String searchText = currentSearchText.toLowerCase();
            if (!task.getTitle().toLowerCase().contains(searchText) &&
                (task.getDescription() == null || !task.getDescription().toLowerCase().contains(searchText))) {
                return false;
            }
        }
        
        return true;
    }

    private int compareGoals(Goal g1, Goal g2) {
        switch (currentSortBy) {
            case "Due Date":
                if (g1.getTargetDate() == null && g2.getTargetDate() == null) return 0;
                if (g1.getTargetDate() == null) return 1;
                if (g2.getTargetDate() == null) return -1;
                return g1.getTargetDate().compareTo(g2.getTargetDate());
            case "Title":
                return g1.getTitle().compareTo(g2.getTitle());
            default:
                return g1.getTitle().compareTo(g2.getTitle());
        }
    }

    private int compareTasks(Task t1, Task t2) {
        switch (currentSortBy) {
            case "Due Date":
                if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                if (t1.getDueDate() == null) return 1;
                if (t2.getDueDate() == null) return -1;
                return t1.getDueDate().compareTo(t2.getDueDate());
            case "Priority":
                return t2.getPriority().compareTo(t1.getPriority()); // High to Low
            case "Status":
                return t1.getStatus().compareTo(t2.getStatus());
            case "Title":
                return t1.getTitle().compareTo(t2.getTitle());
            default:
                return t1.getTitle().compareTo(t2.getTitle());
        }
    }

    private String getPriorityIcon(Task.Priority priority) {
        switch (priority) {
            case HIGH: return "🔴";
            case URGENT: return "🚨";
            case MEDIUM: return "🟡";
            case LOW: return "🟢";
            default: return "⚪";
        }
    }
    
    private String getPriorityColor(Task.Priority priority) {
        switch (priority) {
            case HIGH: return "linear-gradient(to right, #e74c3c, #c0392b)";
            case URGENT: return "linear-gradient(to right, #8e44ad, #7b1fa2)";
            case MEDIUM: return "linear-gradient(to right, #f39c12, #e67e22)";
            case LOW: return "linear-gradient(to right, #27ae60, #229954)";
            default: return "linear-gradient(to right, #95a5a6, #7f8c8d)";
        }
    }
    
    private String getStatusColor(Task.TaskStatus status) {
        switch (status) {
            case TO_DO: return "linear-gradient(to right, #3498db, #2980b9)";
            case IN_PROGRESS: return "linear-gradient(to right, #f39c12, #e67e22)";
            case DONE: return "linear-gradient(to right, #27ae60, #229954)";
            default: return "linear-gradient(to right, #95a5a6, #7f8c8d)";
        }
    }

    private String getStatusIcon(Task.TaskStatus status) {
        switch (status) {
            case TO_DO: return "📋";
            case IN_PROGRESS: return "🔄";
            case DONE: return "✅";
            default: return "❓";
        }
    }

    // Dialog methods for CRUD operations
    private void showAddGoalDialog() {
        GoalEditDialog dialog = new GoalEditDialog(null, goalService);
        dialog.showAndWait().ifPresent(goal -> {
            goalService.addGoal(goal);
            refreshContent();
        });
    }

    private void showEditGoalDialog(Goal goal) {
        GoalEditDialog dialog = new GoalEditDialog(goal, goalService);
        dialog.showAndWait().ifPresent(updatedGoal -> {
            // Goal is already updated by reference
            refreshContent();
        });
    }

    private void showDeleteGoalConfirmation(Goal goal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Goal");
        alert.setHeaderText("Delete Goal: " + goal.getTitle());
        alert.setContentText("Are you sure you want to delete this goal? This action cannot be undone.\n\n" +
                "Note: All child goals and associated tasks will also be deleted.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                goalService.deleteGoal(goal);
                refreshContent();
            }
        });
    }

    private void showAddTaskDialog() {
        TaskEditDialog dialog = new TaskEditDialog(null, goalService);
        dialog.showAndWait().ifPresent(task -> {
            // Add task to a goal - for now, add to the first available goal
            List<Goal> goals = goalService.getAllGoals();
            if (!goals.isEmpty()) {
                goals.get(0).addTask(task);
            }
            refreshContent();
        });
    }

    private void showEditTaskDialog(Task task) {
        TaskEditDialog dialog = new TaskEditDialog(task, goalService);
        dialog.showAndWait().ifPresent(updatedTask -> {
            // Task is already updated by reference
            refreshContent();
        });
    }

    private void showDeleteTaskConfirmation(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Delete Task: " + task.getTitle());
        alert.setContentText("Are you sure you want to delete this task? This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Find the goal containing this task and remove it
                for (Goal goal : goalService.getAllGoals()) {
                    if (goal.getTasks().contains(task)) {
                        goal.removeTask(task);
                        break;
                    }
                }
                refreshContent();
            }
        });
    }

    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        applyTheme();
    }

    private void applyTheme() {
        this.getStylesheets().clear();
        
        // Try to load existing CSS file
        try {
            var cssResource = getClass().getResource("/icons/mainview.css");
            if (cssResource != null) {
                this.getStylesheets().add(cssResource.toExternalForm());
            }
        } catch (Exception e) {
            // CSS file not found, continue with inline styles
            System.out.println("CSS file not found, using inline styles");
        }
        
        // Apply inline styles for task management specific styling
        this.setStyle(isDarkMode ? 
            "-fx-background-color: #2b2b2b; -fx-text-fill: white;" : 
            "-fx-background-color: #f5f5f5; -fx-text-fill: black;");
    }

    /**
     * Simple Goal Edit Dialog for creating and editing goals
     */
    private static class GoalEditDialog extends Dialog<Goal> {
        private final Goal originalGoal;
        private final GoalService goalService;
        
        private TextField titleField;
        private TextArea descriptionArea;
        private ComboBox<GoalType> typeComboBox;
        private ComboBox<Goal> parentGoalComboBox;
        private DatePicker targetDatePicker;
        private CheckBox completedCheckBox;

        public GoalEditDialog(Goal goal, GoalService goalService) {
            this.originalGoal = goal;
            this.goalService = goalService;
            
            setTitle(goal == null ? "Add New Goal" : "Edit Goal");
            setHeaderText(goal == null ? "Create a new goal" : "Edit goal: " + goal.getTitle());
            
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            createForm();
            
            if (goal != null) {
                populateFields(goal);
            }
            
            setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return createGoalFromForm();
                }
                return null;
            });
        }

        private void createForm() {
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            titleField = new TextField();
            titleField.setPromptText("Goal title");
            titleField.setPrefWidth(300);

            descriptionArea = new TextArea();
            descriptionArea.setPromptText("Goal description");
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setPrefWidth(300);

            typeComboBox = new ComboBox<>(FXCollections.observableArrayList(GoalType.values()));

            parentGoalComboBox = new ComboBox<>();
            parentGoalComboBox.getItems().add(null); // No parent option
            parentGoalComboBox.getItems().addAll(goalService.getAllGoals());
            parentGoalComboBox.setConverter(new StringConverter<Goal>() {
                @Override
                public String toString(Goal goal) {
                    return goal == null ? "No Parent" : goal.getTitle();
                }
                @Override
                public Goal fromString(String string) {
                    return null; // Not used
                }
            });

            targetDatePicker = new DatePicker();
            completedCheckBox = new CheckBox();

            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionArea, 1, 1);
            grid.add(new Label("Type:"), 0, 2);
            grid.add(typeComboBox, 1, 2);
            grid.add(new Label("Parent Goal:"), 0, 3);
            grid.add(parentGoalComboBox, 1, 3);
            grid.add(new Label("Target Date:"), 0, 4);
            grid.add(targetDatePicker, 1, 4);
            grid.add(new Label("Completed:"), 0, 5);
            grid.add(completedCheckBox, 1, 5);

            getDialogPane().setContent(grid);
        }

        private void populateFields(Goal goal) {
            titleField.setText(goal.getTitle());
            descriptionArea.setText(goal.getDescription());
            typeComboBox.setValue(goal.getType());
            targetDatePicker.setValue(goal.getTargetDate());
            completedCheckBox.setSelected(goal.isCompleted());
            
            if (goal.getParentGoal() != null) {
                parentGoalComboBox.setValue(goal.getParentGoal());
            }
        }

        private Goal createGoalFromForm() {
            if (originalGoal != null) {
                // Edit existing goal
                originalGoal.setTitle(titleField.getText());
                originalGoal.setDescription(descriptionArea.getText());
                originalGoal.setType(typeComboBox.getValue());
                originalGoal.setTargetDate(targetDatePicker.getValue());
                originalGoal.setCompleted(completedCheckBox.isSelected());
                
                Goal parentGoal = parentGoalComboBox.getValue();
                if (parentGoal != null && originalGoal.getParentGoal() != parentGoal) {
                    if (originalGoal.getParentGoal() != null) {
                        originalGoal.getParentGoal().removeSubGoal(originalGoal);
                    }
                    parentGoal.addSubGoal(originalGoal);
                }
                
                return originalGoal;
            } else {
                // Create new goal
                Goal parentGoal = parentGoalComboBox.getValue();
                Goal newGoal = new Goal(
                    titleField.getText(),
                    typeComboBox.getValue() != null ? typeComboBox.getValue() : GoalType.SHORT_TERM,
                    targetDatePicker.getValue() != null ? targetDatePicker.getValue() : LocalDate.now().plusDays(30),
                    parentGoal
                );
                newGoal.setDescription(descriptionArea.getText());
                newGoal.setCompleted(completedCheckBox.isSelected());
                
                return newGoal;
            }
        }
    }

    /**
     * Simple Task Edit Dialog for creating and editing tasks
     */
    private static class TaskEditDialog extends Dialog<Task> {
        private final Task originalTask;
        private final GoalService goalService;
        
        private TextField titleField;
        private TextArea descriptionArea;
        private ComboBox<Task.Priority> priorityComboBox;
        private ComboBox<Task.TaskStatus> statusComboBox;
        private ComboBox<Goal> goalComboBox;
        private DatePicker dueDatePicker;

        public TaskEditDialog(Task task, GoalService goalService) {
            this.originalTask = task;
            this.goalService = goalService;
            
            setTitle(task == null ? "Add New Task" : "Edit Task");
            setHeaderText(task == null ? "Create a new task" : "Edit task: " + task.getTitle());
            
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            createForm();
            
            if (task != null) {
                populateFields(task);
            }
            
            setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return createTaskFromForm();
                }
                return null;
            });
        }

        private void createForm() {
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            titleField = new TextField();
            titleField.setPromptText("Task title");
            titleField.setPrefWidth(300);

            descriptionArea = new TextArea();
            descriptionArea.setPromptText("Task description");
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setPrefWidth(300);

            priorityComboBox = new ComboBox<>(FXCollections.observableArrayList(Task.Priority.values()));
            priorityComboBox.setValue(Task.Priority.MEDIUM);

            statusComboBox = new ComboBox<>(FXCollections.observableArrayList(Task.TaskStatus.values()));
            statusComboBox.setValue(Task.TaskStatus.TO_DO);

            goalComboBox = new ComboBox<>();
            goalComboBox.getItems().add(null); // No goal option
            goalComboBox.getItems().addAll(goalService.getAllGoals());
            goalComboBox.setConverter(new StringConverter<Goal>() {
                @Override
                public String toString(Goal goal) {
                    return goal == null ? "No Goal" : goal.getTitle();
                }
                @Override
                public Goal fromString(String string) {
                    return null; // Not used
                }
            });

            dueDatePicker = new DatePicker();

            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionArea, 1, 1);
            grid.add(new Label("Priority:"), 0, 2);
            grid.add(priorityComboBox, 1, 2);
            grid.add(new Label("Status:"), 0, 3);
            grid.add(statusComboBox, 1, 3);
            grid.add(new Label("Associated Goal:"), 0, 4);
            grid.add(goalComboBox, 1, 4);
            grid.add(new Label("Due Date:"), 0, 5);
            grid.add(dueDatePicker, 1, 5);

            getDialogPane().setContent(grid);
        }

        private void populateFields(Task task) {
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            priorityComboBox.setValue(task.getPriority());
            statusComboBox.setValue(task.getStatus());
            dueDatePicker.setValue(task.getDueDate());
        }

        private Task createTaskFromForm() {
            if (originalTask != null) {
                // Edit existing task - update fields directly since Task doesn't have setTitle
                originalTask.setDescription(descriptionArea.getText());
                originalTask.setPriority(priorityComboBox.getValue());
                originalTask.setStatus(statusComboBox.getValue());
                originalTask.setDueDate(dueDatePicker.getValue());
                
                return originalTask;
            } else {
                // Create new task using available constructor
                Task newTask = new Task(
                    titleField.getText(),
                    descriptionArea.getText(),
                    dueDatePicker.getValue() != null ? dueDatePicker.getValue() : LocalDate.now().plusDays(7)
                );
                newTask.setPriority(priorityComboBox.getValue() != null ? priorityComboBox.getValue() : Task.Priority.MEDIUM);
                newTask.setStatus(statusComboBox.getValue() != null ? statusComboBox.getValue() : Task.TaskStatus.TO_DO);
                
                return newTask;
            }
        }
    }
}
