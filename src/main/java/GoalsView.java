import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.scene.input.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import model.Goal;
import model.GoalType;
import service.GoalService;
// import util.DragAndDropHelper;
import util.AppLogger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Timer;
import java.util.TimerTask;

public class GoalsView {
    private static final java.util.logging.Logger logger = AppLogger.getLogger();

    private final GoalService service;
    private VBox leftColumn;
    private VBox rightColumn;
    private TreeView<Goal> hierarchyTreeView;
    private TabPane goalTabPane;
    private Goal selectedGoal;
    
    // // Search and filter functionality
    // private TextField searchField;
    // private ComboBox<String> filterCombo;
    // private FilteredList<Goal> filteredGoals;
    // private ObservableList<Goal> allGoals;
    
    // // Auto-save functionality
    // private final AtomicBoolean hasUnsavedChanges = new AtomicBoolean(false);
    // private Timer autoSaveTimer;
    // private Label saveStatusLabel;
    // private Circle saveStatusIndicator;
    
    // // Statistics dashboard
    // private VBox statsPanel;
    // private boolean statsVisible = false;
    
    // Drag and drop helper
    // private DragAndDropHelper dragDropHelper;

    public GoalsView(GoalService service) {
        this.service = service;
        // this.dragDropHelper = new DragAndDropHelper(service);
        logger.info("GoalsView initialized");
    }

    public void buildScreen(Stage primaryStage) {
        logger.info("Building GoalsView screen");
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        
        // Apply CSS styling
        Scene scene = new Scene(mainLayout, 1200, 800);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/modern-goals.css").toExternalForm());
        } catch (Exception e) {
            // Fallback to inline styles if CSS file not found
            mainLayout.setStyle("-fx-background-color: #f8fafc;");
        }

        // Create modern header
        HBox header = createModernHeader(primaryStage);
        mainLayout.setTop(header);

        // Create tabbed interface for different view modes
        goalTabPane = new TabPane();
        goalTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        goalTabPane.setStyle("-fx-background-color: transparent;");

        // Tab 1: Hierarchical View
        Tab hierarchicalTab = new Tab("🏗️ Hierarchical Goals");
        hierarchicalTab.setContent(createHierarchicalView());

        // Tab 2: List View (original design enhanced)
        Tab listTab = new Tab("📋 All Goals");
        listTab.setContent(createListView());

        // Tab 3: Board View (Kanban-style)
        Tab boardTab = new Tab("📊 Goal Board");
        boardTab.setContent(createBoardView());

        goalTabPane.getTabs().addAll(hierarchicalTab, listTab, boardTab);
        mainLayout.setCenter(goalTabPane);

        // Create footer with actions
        HBox footer = createFooter();
        mainLayout.setBottom(footer);

        primaryStage.setTitle("🎯 Goal Tracker - Modern View");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createModernHeader(Stage primaryStage) {
        HBox header = new HBox(20);
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: transparent;");

        // Title with icon
        Text title = new Text("🎯 Goal Tracker");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-fill: #1a202c;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action buttons
        Button addGoalBtn = new Button("+ New Goal");
        addGoalBtn.setStyle(
            "-fx-background-color: #667eea;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 12 24 12 24;"
        );
        addGoalBtn.setOnAction(e -> {
            // Open add goal dialog
            openAddGoalDialog(primaryStage);
        });

        Button goBackBtn = new Button("← Back");
        goBackBtn.setStyle(
            "-fx-background-color: #f7fafc;" +
            "-fx-text-fill: #4a5568;" +
            "-fx-font-size: 14px;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 12 24 12 24;"
        );
        goBackBtn.setOnAction(e -> {
            // Go back to main view
            try {
                MainView mainView = new MainView();
                mainView.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        header.getChildren().addAll(title, spacer, addGoalBtn, goBackBtn);
        return header;
    }

    private void openAddGoalDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Create New Goal");
        dialog.initOwner(parentStage);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Goal title");

        TextArea descField = new TextArea();
        descField.setPromptText("Goal description");
        descField.setPrefRowCount(3);

        ComboBox<GoalType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(GoalType.values());
        typeCombo.setValue(GoalType.SHORT_TERM);

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now().plusMonths(1));

        Button createBtn = new Button("Create Goal");
        createBtn.setStyle(
            "-fx-background-color: #48bb78;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;"
        );

        createBtn.setOnAction(e -> {
            if (!titleField.getText().trim().isEmpty()) {
                Goal newGoal = new Goal(
                    titleField.getText().trim(),
                    descField.getText().trim(),
                    typeCombo.getValue(),
                    datePicker.getValue(),
                    null
                );
                service.addGoal(newGoal);
                refreshAllViews();
                dialog.close();
            }
        });

        layout.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Description:"), descField,
            new Label("Type:"), typeCombo,
            new Label("Target Date:"), datePicker,
            createBtn
        );

        Scene dialogScene = new Scene(layout, 400, 350);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private ScrollPane createHierarchicalView() {
        VBox hierarchyContainer = new VBox(15);
        hierarchyContainer.setPadding(new Insets(20));
        hierarchyContainer.setStyle("-fx-background-color: transparent;");

        // Create tree view for hierarchical goals
        hierarchyTreeView = createGoalTreeView();
        
        // Split pane for tree and details
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.4);

        VBox treeContainer = new VBox(10);
        treeContainer.setPadding(new Insets(15));
        treeContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.3, 0, 2);"
        );

        Text treeTitle = new Text("📋 Goal Hierarchy");
        treeTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2d3748;");
        
        treeContainer.getChildren().addAll(treeTitle, hierarchyTreeView);
        VBox.setVgrow(hierarchyTreeView, Priority.ALWAYS);

        // Details panel
        VBox detailsContainer = new VBox(10);
        rightColumn = new VBox(15);
        rightColumn.setPadding(new Insets(15));
        detailsContainer.getChildren().add(rightColumn);
        detailsContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.3, 0, 2);"
        );

        Text detailsTitle = new Text("📝 Goal Details & Tasks");
        detailsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2d3748;");
        
        ScrollPane detailsScroll = new ScrollPane(rightColumn);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setStyle("-fx-background-color: transparent;");
        
        detailsContainer.getChildren().addAll(detailsTitle, detailsScroll);
        VBox.setVgrow(detailsScroll, Priority.ALWAYS);

        splitPane.getItems().addAll(treeContainer, detailsContainer);
        
        ScrollPane hierarchyScroll = new ScrollPane(splitPane);
        hierarchyScroll.setFitToWidth(true);
        hierarchyScroll.setFitToHeight(true);
        hierarchyScroll.setStyle("-fx-background-color: transparent;");
        
        return hierarchyScroll;
    }

    private TreeView<Goal> createGoalTreeView() {
        TreeView<Goal> treeView = new TreeView<>();
        treeView.setStyle("-fx-background-color: transparent;");
        
        TreeItem<Goal> rootItem = new TreeItem<>(null);
        rootItem.setExpanded(true);
        
        // Get hierarchical goals and build tree
        List<Goal> longTermGoals = service.getAllGoals().stream()
            .filter(goal -> goal.getType() == GoalType.LONG_TERM_GOAL)
            .collect(Collectors.toList());
            
        for (Goal longTermGoal : longTermGoals) {
            TreeItem<Goal> longTermItem = new TreeItem<>(longTermGoal);
            longTermItem.setExpanded(true);
            
            // Add year goals
            List<Goal> yearGoals = longTermGoal.getChildGoals().stream()
                .filter(goal -> goal.getType() == GoalType.YEAR_GOAL)
                .collect(Collectors.toList());
                
            for (Goal yearGoal : yearGoals) {
                TreeItem<Goal> yearItem = new TreeItem<>(yearGoal);
                yearItem.setExpanded(true);
                
                // Add month goals
                List<Goal> monthGoals = yearGoal.getChildGoals().stream()
                    .filter(goal -> goal.getType() == GoalType.MONTH_GOAL)
                    .collect(Collectors.toList());
                    
                for (Goal monthGoal : monthGoals) {
                    TreeItem<Goal> monthItem = new TreeItem<>(monthGoal);
                    yearItem.getChildren().add(monthItem);
                }
                
                longTermItem.getChildren().add(yearItem);
            }
            
            rootItem.getChildren().add(longTermItem);
        }
        
        // Add non-hierarchical goals
        List<Goal> otherGoals = service.getAllGoals().stream()
            .filter(goal -> goal.getType() != GoalType.LONG_TERM_GOAL && 
                          goal.getType() != GoalType.YEAR_GOAL && 
                          goal.getType() != GoalType.MONTH_GOAL)
            .collect(Collectors.toList());
            
        for (Goal goal : otherGoals) {
            TreeItem<Goal> goalItem = new TreeItem<>(goal);
            rootItem.getChildren().add(goalItem);
        }
        
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);
        
        // Custom cell factory for better display
        treeView.setCellFactory(tv -> new TreeCell<Goal>() {
            @Override
            protected void updateItem(Goal goal, boolean empty) {
                super.updateItem(goal, empty);
                if (empty || goal == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cellContent = new HBox(8);
                    cellContent.setAlignment(Pos.CENTER_LEFT);
                    
                    // Icon based on goal type
                    Text icon = new Text(getGoalIcon(goal.getType()));
                    icon.setStyle("-fx-font-size: 14px;");
                    
                    // Goal title
                    Text title = new Text(goal.getTitle());
                    title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                    
                    // Progress indicator
                    ProgressBar progressBar = new ProgressBar(goal.getProgress() / 100.0);
                    progressBar.setPrefWidth(60);
                    progressBar.setPrefHeight(8);
                    
                    Text progressText = new Text(String.format("%.0f%%", goal.getProgress()));
                    progressText.setStyle("-fx-font-size: 10px; -fx-fill: #718096;");
                    
                    cellContent.getChildren().addAll(icon, title, progressBar, progressText);
                    setGraphic(cellContent);
                    setText(null);
                }
            }
        });
        
        // Handle selection
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getValue() != null) {
                selectedGoal = newSelection.getValue();
                updateRightColumn(selectedGoal);
            }
        });
        
        return treeView;
    }

    private String getGoalIcon(GoalType type) {
        switch (type) {
            case LONG_TERM_GOAL: return "🎯";
            case YEAR_GOAL: return "📅";
            case MONTH_GOAL: return "🗓️";
            case WEEKLY: return "📋";
            case DAILY: return "📝";
            case MONTHLY: return "📊";
            case YEARLY: return "🎊";
            case SHORT_TERM: return "⚡";
            case LONG_TERM: return "🏔️";
            default: return "📌";
        }
    }

    private ScrollPane createListView() {
        // Enhanced left column with better design
        leftColumn = createEnhancedLeftColumn();
        
        // Enhanced right column 
        VBox rightContainer = new VBox(15);
        rightContainer.setPadding(new Insets(15));
        rightContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.3, 0, 2);"
        );
        
        rightColumn = new VBox(15);
        ScrollPane rightScroll = new ScrollPane(rightColumn);
        rightScroll.setFitToWidth(true);
        rightScroll.setStyle("-fx-background-color: transparent;");
        
        Text rightTitle = new Text("📝 Tasks & Details");
        rightTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2d3748;");
        
        rightContainer.getChildren().addAll(rightTitle, rightScroll);
        VBox.setVgrow(rightScroll, Priority.ALWAYS);
        
        // Split pane layout
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.4);
        splitPane.getItems().addAll(leftColumn, rightContainer);
        
        // Show tasks of the first goal if available
        List<Goal> allGoals = service.getAllGoals();
        if (!allGoals.isEmpty()) {
            updateRightColumn(allGoals.get(0));
        }
        
        ScrollPane listScroll = new ScrollPane(splitPane);
        listScroll.setFitToWidth(true);
        listScroll.setFitToHeight(true);
        listScroll.setStyle("-fx-background-color: transparent;");
        
        return listScroll;
    }

    private VBox createEnhancedLeftColumn() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));
        container.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.3, 0, 2);"
        );
        
        Text title = new Text("🎯 Your Goals");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2d3748;");
        
        ScrollPane scrollPane = new ScrollPane();
        leftColumn = new VBox(10);
        scrollPane.setContent(leftColumn);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        // Populate with goals
        updateLeftColumn();
        
        container.getChildren().addAll(title, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        return container;
    }

    private ScrollPane createBoardView() {
        VBox boardContainer = new VBox(20);
        boardContainer.setPadding(new Insets(20));
        
        // Create Kanban-style board
        HBox board = new HBox(20);
        board.setAlignment(Pos.TOP_CENTER);
        
        // Group goals by type
        VBox longTermColumn = createGoalColumn("🎯 Long-term Goals", GoalType.LONG_TERM_GOAL, "#e8f5e8");
        VBox yearColumn = createGoalColumn("📅 Year Goals", GoalType.YEAR_GOAL, "#fff3e0");
        VBox monthColumn = createGoalColumn("🗓️ Month Goals", GoalType.MONTH_GOAL, "#f3e5f5");
        VBox otherColumn = createGoalColumn("📌 Other Goals", null, "#f8fafc");
        
        board.getChildren().addAll(longTermColumn, yearColumn, monthColumn, otherColumn);
        HBox.setHgrow(longTermColumn, Priority.ALWAYS);
        HBox.setHgrow(yearColumn, Priority.ALWAYS);
        HBox.setHgrow(monthColumn, Priority.ALWAYS);
        HBox.setHgrow(otherColumn, Priority.ALWAYS);
        
        ScrollPane boardScroll = new ScrollPane(board);
        boardScroll.setFitToWidth(true);
        boardScroll.setFitToHeight(true);
        boardScroll.setStyle("-fx-background-color: transparent;");
        
        return boardScroll;
    }

    private VBox createGoalColumn(String title, GoalType filterType, String bgColor) {
        VBox column = new VBox(10);
        column.setPadding(new Insets(15));
        column.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.3, 0, 2);"
        );
        column.setMinWidth(280);
        
        Text columnTitle = new Text(title);
        columnTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #2d3748;");
        
        ScrollPane columnScroll = new ScrollPane();
        VBox goalsContainer = new VBox(10);
        columnScroll.setContent(goalsContainer);
        columnScroll.setFitToWidth(true);
        columnScroll.setStyle("-fx-background-color: transparent;");
        
        // Filter and add goals
        List<Goal> filteredGoals;
        if (filterType != null) {
            filteredGoals = service.getAllGoals().stream()
                .filter(goal -> goal.getType() == filterType)
                .collect(Collectors.toList());
        } else {
            filteredGoals = service.getAllGoals().stream()
                .filter(goal -> goal.getType() != GoalType.LONG_TERM_GOAL && 
                              goal.getType() != GoalType.YEAR_GOAL && 
                              goal.getType() != GoalType.MONTH_GOAL)
                .collect(Collectors.toList());
        }
        
        for (Goal goal : filteredGoals) {
            VBox goalCard = createGoalCard(goal);
            goalsContainer.getChildren().add(goalCard);
        }
        
        column.getChildren().addAll(columnTitle, columnScroll);
        VBox.setVgrow(columnScroll, Priority.ALWAYS);
        
        return column;
    }

    private VBox createGoalCard(Goal goal) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.2, 0, 1);"
        );
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #f7fafc;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: #cbd5e0;" +
            "-fx-border-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 2);"
        ));
        
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.2, 0, 1);"
        ));
        
        // Title
        Text title = new Text(goal.getTitle());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #2d3748;");
        title.setWrappingWidth(240);
        
        // Progress
        ProgressBar progressBar = new ProgressBar(goal.getProgress() / 100.0);
        progressBar.setPrefWidth(240);
        progressBar.setPrefHeight(6);
        
        HBox progressInfo = new HBox();
        progressInfo.setAlignment(Pos.CENTER_LEFT);
        Text progressText = new Text(String.format("%.0f%% Complete", goal.getProgress()));
        progressText.setStyle("-fx-font-size: 11px; -fx-fill: #718096;");
        progressInfo.getChildren().add(progressText);
        
        // Due date
        Text dueDate = new Text("Due: " + (goal.getTargetDate() != null ? goal.getTargetDate().toString() : "No date"));
        dueDate.setStyle("-fx-font-size: 11px; -fx-fill: #a0aec0;");
        
        // Task count
        Text taskCount = new Text(goal.getTasks().size() + " tasks");
        taskCount.setStyle("-fx-font-size: 11px; -fx-fill: #a0aec0;");
        
        card.getChildren().addAll(title, progressBar, progressInfo, dueDate, taskCount);
        
        // Click handler
        card.setOnMouseClicked(e -> {
            selectedGoal = goal;
            updateRightColumn(goal);
        });
        
        return card;
    }

    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(20, 0, 0, 0));
        footer.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveButton = new Button("💾 Save to Excel");
        saveButton.setStyle(
            "-fx-background-color: #48bb78;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 12 24 12 24;"
        );
        saveButton.setOnAction(e -> {
            service.saveGoalsToFile();
            // Show save confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Goals saved successfully!");
            alert.showAndWait();
        });
        
        Button loadButton = new Button("📂 Load from Excel");
        loadButton.setStyle(
            "-fx-background-color: #4299e1;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 12 24 12 24;"
        );
        loadButton.setOnAction(e -> {
            service.loadGoalsFromFile();
            // Refresh all views
            refreshAllViews();
            // Show load confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Goals loaded successfully!");
            alert.showAndWait();
        });
        
        footer.getChildren().addAll(loadButton, saveButton);
        return footer;
    }

    // Update methods for enhanced goal display
    private void updateLeftColumn() {
        if (leftColumn == null) return;

        leftColumn.getChildren().clear();
        List<Goal> allGoals = service.getAllGoals();
        logger.info("Loaded " + allGoals.size() + " goals for left column");

        // Sort goals by type, then by due date
        allGoals.sort((g1, g2) -> {
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

        for (Goal goal : allGoals) {
            VBox goalContainer = createModernGoalCard(goal);
            leftColumn.getChildren().add(goalContainer);
        }
    }

    private VBox createModernGoalCard(Goal goal) {
        VBox goalContainer = new VBox(8);
        goalContainer.setPadding(new Insets(15));
        goalContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0.2, 0, 2);"
        );

        // Hover effect
        goalContainer.setOnMouseEntered(e -> goalContainer.setStyle(
            "-fx-background-color: #f8fafc;" +
            "-fx-border-color: #a0aec0;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0.3, 0, 3);"
        ));
        goalContainer.setOnMouseExited(e -> goalContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0.2, 0, 2);"
        ));

        // Header with icon and title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Text icon = new Text(getGoalIcon(goal.getType()));
        icon.setStyle("-fx-font-size: 16px;");
        
        Text goalTitleText = new Text(goal.getTitle());
        goalTitleText.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-fill: #2d3748;");
        goalTitleText.setWrappingWidth(180);
        
        header.getChildren().addAll(icon, goalTitleText);

        // Goal type badge with enhanced styling
        Label typeLabel = new Label(goal.getType().toString().replace("_", " "));
        typeLabel.setStyle(
            "-fx-background-color: #f7fafc;" +
            "-fx-text-fill: #4a5568;" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 4 8 4 8;" +
            "-fx-background-radius: 12px;"
        );
        
        // Enhanced color coding for goal types
        switch (goal.getType()) {
            case LONG_TERM_GOAL:
                typeLabel.setStyle(typeLabel.getStyle() + "-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32;");
                break;
            case YEAR_GOAL:
                typeLabel.setStyle(typeLabel.getStyle() + "-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00;");
                break;
            case MONTH_GOAL:
                typeLabel.setStyle(typeLabel.getStyle() + "-fx-background-color: #f3e5f5; -fx-text-fill: #7b1fa2;");
                break;
            case WEEKLY:
                typeLabel.setStyle(typeLabel.getStyle() + "-fx-background-color: #e1f5fe; -fx-text-fill: #0277bd;");
                break;
            case MONTHLY:
                typeLabel.setStyle(typeLabel.getStyle() + "-fx-background-color: #fce4ec; -fx-text-fill: #c2185b;");
                break;
            case YEARLY:
                typeLabel.setStyle(typeLabel.getStyle() + "-fx-background-color: #ede7f6; -fx-text-fill: #5e35b1;");
                break;
            case DAILY:
                typeLabel.setStyle(typeLabel.getStyle() + "-fx-background-color: #fffde7; -fx-text-fill: #f57f17;");
                break;
            case SHORT_TERM:
                typeLabel.setStyle(typeLabel.getStyle() + "-fx-background-color: #ffe0b2; -fx-text-fill: #e65100;");
                break;
            case LONG_TERM:
                typeLabel.setStyle(typeLabel.getStyle() + "-fx-background-color: #d1c4e9; -fx-text-fill: #4527a0;");
                break;
        }

        // Enhanced progress section
        ProgressBar progressBar = new ProgressBar(goal.getProgress() / 100.0);
        progressBar.setPrefWidth(200);
        progressBar.setPrefHeight(8);
        progressBar.setStyle("-fx-accent: #48bb78;");

        HBox progressInfo = new HBox();
        progressInfo.setAlignment(Pos.CENTER_LEFT);
        progressInfo.setSpacing(5);
        
        Text progressText = new Text(String.format("%.1f%%", goal.getProgress()));
        progressText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: #48bb78;");
        
        Text completedTasks = new Text("(" + goal.getTasks().stream().mapToInt(t -> t.isCompleted() ? 1 : 0).sum() + "/" + goal.getTasks().size() + " tasks)");
        completedTasks.setStyle("-fx-font-size: 10px; -fx-fill: #718096;");
        
        progressInfo.getChildren().addAll(progressText, completedTasks);

        // Due date with status indicator
        Text dueDate = new Text("Due: " + (goal.getTargetDate() != null ? goal.getTargetDate().toString() : "No date"));
        if (goal.getTargetDate() != null && goal.getTargetDate().isBefore(LocalDate.now()) && goal.getProgress() < 100) {
            dueDate.setStyle("-fx-font-size: 11px; -fx-fill: #e53e3e; -fx-font-weight: bold;"); // Overdue
        } else {
            dueDate.setStyle("-fx-font-size: 11px; -fx-fill: #718096;");
        }

        goalContainer.getChildren().addAll(header, typeLabel, progressBar, progressInfo, dueDate);
        goalContainer.setOnMouseClicked(event -> updateRightColumn(goal));
        
        return goalContainer;
    }

    public void updateRightColumn(Goal selectedGoal) {
        if (rightColumn == null) return;
        logger.info("Updating right column for goal: " + selectedGoal.getTitle());
        rightColumn.getChildren().clear();
        
        // Goal header
        VBox goalHeader = new VBox(10);
        goalHeader.setPadding(new Insets(0, 0, 15, 0));
        goalHeader.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Text goalIcon = new Text(getGoalIcon(selectedGoal.getType()));
        goalIcon.setStyle("-fx-font-size: 24px;");
        
        Text goalTitle = new Text(selectedGoal.getTitle());
        goalTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-fill: #2d3748;");
        goalTitle.setWrappingWidth(300);
        
        titleRow.getChildren().addAll(goalIcon, goalTitle);
        
        // Goal progress summary
        ProgressBar mainProgress = new ProgressBar(selectedGoal.getProgress() / 100.0);
        mainProgress.setPrefWidth(350);
        mainProgress.setPrefHeight(12);
        mainProgress.setStyle("-fx-accent: #48bb78;");
        
        Text progressSummary = new Text(String.format("%.1f%% Complete (%d/%d tasks)", 
            selectedGoal.getProgress(), 
            (int)selectedGoal.getTasks().stream().filter(model.Task::isCompleted).count(),
            selectedGoal.getTasks().size()));
        progressSummary.setStyle("-fx-font-size: 14px; -fx-fill: #4a5568;");
        
        goalHeader.getChildren().addAll(titleRow, mainProgress, progressSummary);
        rightColumn.getChildren().add(goalHeader);
        
        // Tasks section
        List<model.Task> tasks = selectedGoal.getTasks();

        // Sort tasks by status and priority
        tasks.sort((task1, task2) -> {
            int statusOrder1 = getStatusOrder(task1.getStatus());
            int statusOrder2 = getStatusOrder(task2.getStatus());
            if (statusOrder1 != statusOrder2) {
                return Integer.compare(statusOrder1, statusOrder2);
            }
            if (task1.getStatus() != model.Task.TaskStatus.DONE && task2.getStatus() != model.Task.TaskStatus.DONE) {
                int p1 = getPriorityOrder(task1.getPriority());
                int p2 = getPriorityOrder(task2.getPriority());
                if (p1 != p2) {
                    return Integer.compare(p1, p2);
                }
            }
            if (task1.getDueDate() != null && task2.getDueDate() != null) {
                return task1.getDueDate().compareTo(task2.getDueDate());
            }
            return 0;
        });

        for (model.Task task : tasks) {
            VBox taskCard = createModernTaskCard(task, selectedGoal);
            rightColumn.getChildren().add(taskCard);
        }

        // Add Task Button
        Button addTaskButton = new Button("+ Add New Task");
        addTaskButton.setStyle(
            "-fx-background-color: #667eea;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 15 30 15 30;"
        );
        addTaskButton.setMaxWidth(Double.MAX_VALUE);
        addTaskButton.setOnAction(e -> {
            AddTaskView addTaskView = new AddTaskView(service);
            addTaskView.buildScreen((Stage) addTaskButton.getScene().getWindow());
        });
        rightColumn.getChildren().add(addTaskButton);
    }

    private VBox createModernTaskCard(model.Task task, Goal parentGoal) {
        VBox taskContainer = new VBox(12);
        taskContainer.setPadding(new Insets(16));
        
        String bgColor = getBackgroundColorByStatus(task.getStatus());
        taskContainer.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0.2, 0, 2);"
        );
        
        taskContainer.setOnMouseEntered(ev -> taskContainer.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-border-color: #cbd5e0;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0.3, 0, 3);"
        ));
        taskContainer.setOnMouseExited(ev -> taskContainer.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0.2, 0, 2);"
        ));

        // Task header with title and status
        HBox taskHeader = new HBox(10);
        taskHeader.setAlignment(Pos.CENTER_LEFT);
        
        Text taskTitle = new Text(task.getTitle());
        taskTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #2d3748;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Status badge
        Label statusBadge = new Label(task.getStatus().toString());
        statusBadge.setStyle(getStatusBadgeStyle(task.getStatus()));
        
        taskHeader.getChildren().addAll(taskTitle, spacer, statusBadge);

        // Task info row
        HBox infoRow = new HBox(15);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        
        Text startText = new Text("Start: " + (task.getStartDay() != null ? task.getStartDay() : "-"));
        startText.setStyle("-fx-font-size: 12px; -fx-fill: #718096;");
        
        Text dueText = new Text("Due: " + (task.getDueDate() != null ? task.getDueDate() : "-"));
        boolean isOverdue = task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now()) && !task.isCompleted();
        if (isOverdue) {
            dueText.setStyle("-fx-font-size: 12px; -fx-fill: #e53e3e; -fx-font-weight: bold;");
        } else {
            dueText.setStyle("-fx-font-size: 12px; -fx-fill: #718096;");
        }
        
        infoRow.getChildren().addAll(startText, dueText);

        // Description field
        TextArea taskDescription = new TextArea(task.getDescription());
        taskDescription.setPromptText("Add task details...");
        taskDescription.setPrefRowCount(2);
        taskDescription.setWrapText(true);
        taskDescription.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        taskDescription.textProperty().addListener((obs, oldText, newText) -> {
            task.setDescription(newText);
            service.saveGoalsToFile();
        });

        // Controls grid
        GridPane controls = new GridPane();
        controls.setHgap(15);
        controls.setVgap(10);
        
        // Priority dropdown with color indicators
        ComboBox<model.Task.Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(model.Task.Priority.values());
        priorityCombo.setValue(task.getPriority());
        priorityCombo.setCellFactory(lv -> new ListCell<model.Task.Priority>() {
            @Override
            protected void updateItem(model.Task.Priority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    Circle circle = new Circle(6);
                    switch (item) {
                        case URGENT: circle.setFill(Color.web("#e53e3e")); break;
                        case HIGH: circle.setFill(Color.web("#dd6b20")); break;
                        case MEDIUM: circle.setFill(Color.web("#d69e2e")); break;
                        case LOW: circle.setFill(Color.web("#38a169")); break;
                    }
                    setGraphic(circle);
                }
            }
        });
        priorityCombo.setButtonCell(priorityCombo.getCellFactory().call(null));
        priorityCombo.setOnAction(e -> {
            task.setPriority(priorityCombo.getValue());
            service.saveGoalsToFile();
        });

        // Status dropdown
        ComboBox<model.Task.TaskStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(model.Task.TaskStatus.values());
        statusCombo.setValue(task.getStatus());
        statusCombo.setOnAction(e -> {
            model.Task.TaskStatus newStatus = statusCombo.getValue();
            task.setStatus(newStatus);
            task.setCompleted(newStatus == model.Task.TaskStatus.DONE);
            service.saveGoalsToFile();
            updateRightColumn(parentGoal);
            refreshAllViews();
        });

        // Date pickers
        DatePicker startDayPicker = new DatePicker(task.getStartDay());
        startDayPicker.setOnAction(e -> {
            task.setStartDay(startDayPicker.getValue());
            service.saveGoalsToFile();
        });

        DatePicker duePicker = new DatePicker(task.getDueDate());
        duePicker.setOnAction(e -> {
            task.setDueDate(duePicker.getValue());
            service.saveGoalsToFile();
            updateRightColumn(parentGoal);
        });

        controls.addRow(0, new Label("Priority:"), priorityCombo, new Label("Status:"), statusCombo);
        controls.addRow(1, new Label("Start:"), startDayPicker, new Label("Due:"), duePicker);

        taskContainer.getChildren().addAll(taskHeader, infoRow, taskDescription, controls);
        return taskContainer;
    }

    private String getBackgroundColorByStatus(model.Task.TaskStatus status) {
        switch (status) {
            case TO_DO: return "#f7fafc";
            case IN_PROGRESS: return "#fffbeb";
            case DONE: return "#f0fff4";
            default: return "#ffffff";
        }
    }

    private String getStatusBadgeStyle(model.Task.TaskStatus status) {
        String baseStyle = "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 8 4 8; -fx-background-radius: 12px;";
        switch (status) {
            case TO_DO:
                return baseStyle + "-fx-background-color: #fed7d7; -fx-text-fill: #c53030;";
            case IN_PROGRESS:
                return baseStyle + "-fx-background-color: #feebc8; -fx-text-fill: #dd6b20;";
            case DONE:
                return baseStyle + "-fx-background-color: #c6f6d5; -fx-text-fill: #38a169;";
            default:
                return baseStyle + "-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568;";
        }
    }

    private void refreshAllViews() {
        // Refresh hierarchical view
        if (hierarchyTreeView != null) {
            hierarchyTreeView.setRoot(null);
            hierarchyTreeView = createGoalTreeView();
        }
        
        // Refresh list view
        if (leftColumn != null) {
            updateLeftColumn();
        }
    }

    // Helper methods
    private int getStatusOrder(model.Task.TaskStatus status) {
        if (status == model.Task.TaskStatus.IN_PROGRESS) return 0;
        if (status == model.Task.TaskStatus.TO_DO) return 1;
        return 2; // DONE
    }

    private int getPriorityOrder(model.Task.Priority priority) {
        switch (priority) {
            case URGENT: return 0;
            case HIGH: return 1;
            case MEDIUM: return 2;
            case LOW: return 3;
        }
        return 4;
    }
}
