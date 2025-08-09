import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import service.GoalService;
import util.DailyScheduler;
import util.MailService;
import util.AppLogger;
import model.ViewType;

import java.io.File;
import javafx.embed.swing.SwingFXUtils;

public class MainView extends Application {
    private static final java.util.logging.Logger logger = AppLogger.getLogger();
    private GoalService service = new GoalService();
    private Stage primaryStage;
    private GoalsView goalsView;
    private boolean isDarkTheme = false; // Theme state
    private VBox mainContent; // Reference to main content for theme switching
    private HBox header; // Reference to header for theme switching
    private VBox sidebar; // Reference to sidebar for theme switching
    private BorderPane mainLayout; // Reference to main layout for switching center content
    private SidebarMenuView sidebarMenuView; // Sidebar menu handler
    private ViewType currentView = ViewType.DASHBOARD; // Track current active view

    public static void main(String[] args) {
        launch(args);
    }

    public MainView() {
        logger.info("MainView initialized");
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        logger.info("MainView started");

        service.loadGoalsFromFile();
        // Daily email reminder at 8:00 AM
        DailyScheduler.scheduleDailyTask(() -> {
            try {
                MailService.sendTaskReminder("quocthien049@gmail.com", service.getTodayTasks());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 8, 0);

        // Create the main layout with BorderPane for header and sidebar
        mainLayout = new BorderPane();
        
        // Create and add modern header
        header = createModernHeader();
        mainLayout.setTop(header);

        // Create and add sidebar
        sidebarMenuView = new SidebarMenuView(this, isDarkTheme);
        sidebar = sidebarMenuView.createSidebar();
        mainLayout.setLeft(sidebar);

        // Create the main content (existing layout)
        mainContent = createMainContent();
        mainLayout.setCenter(mainContent);

        Scene mainScene = new Scene(mainLayout, 1100, 650); // Increased width for sidebar
        primaryStage.setTitle("Goal & Task Manager");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    // Create modern header with branding and navigation
    private HBox createModernHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setStyle(getHeaderStyle());

        // Logo/Icon (using 🎯 emoji as fallback if no logo file exists)
        ImageView logo = new ImageView();
        try {
            Image logoImage = new Image(getClass().getResourceAsStream("/icons/create.png"));
            logo.setImage(logoImage);
            logo.setFitHeight(32);
            logo.setFitWidth(32);
        } catch (Exception e) {
            // Fallback to text icon
            Label logoLabel = new Label("🎯");
            logoLabel.setStyle("-fx-font-size: 24px;");
            header.getChildren().add(logoLabel);
            logo = null;
        }

        if (logo != null) {
            header.getChildren().add(logo);
        }

        // App Title
        Label appTitle = new Label("Goal & Task Manager");
        appTitle.setStyle(getTitleStyle());
        header.getChildren().add(appTitle);

        // Spacer to push items to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().add(spacer);

        // Theme Toggle Button
        Button themeToggle = new Button(isDarkTheme ? "☀️" : "🌙");
        themeToggle.setStyle(getHeaderButtonStyle());
        themeToggle.setTooltip(new Tooltip(isDarkTheme ? "Switch to Light Theme" : "Switch to Dark Theme"));
        themeToggle.setOnAction(e -> toggleTheme(themeToggle));

        // Settings Button
        Button settingsBtn = new Button("⚙️");
        settingsBtn.setStyle(getHeaderButtonStyle());
        settingsBtn.setTooltip(new Tooltip("Settings"));
        settingsBtn.setOnAction(e -> showSettingsDialog());

        // Profile/User Button
        Button profileBtn = new Button("👤");
        profileBtn.setStyle(getHeaderButtonStyle());
        profileBtn.setTooltip(new Tooltip("User Profile"));
        profileBtn.setOnAction(e -> showProfileDialog());

        header.getChildren().addAll(themeToggle, settingsBtn, profileBtn);
        return header;
    }

    // Placeholder for future reports view
    public void showReportsView() {
        showInfoDialog("Reports", "Reports feature coming soon!");
    }

    // Getter and setter for currentView (needed by SidebarMenuView)
    public ViewType getCurrentView() {
        return currentView;
    }

    public void setCurrentView(ViewType viewType) {
        this.currentView = viewType;
    }
    
    // Getter for isDarkTheme (needed by SidebarMenuView)
    public boolean isDarkTheme() {
        return isDarkTheme;
    }

    // Show dashboard (main menu) - make public so SidebarMenuView can access
    public void showDashboard() {
        // Create new dashboard view and set it as center content
        DashboardRightView dashboardView = new DashboardRightView(service, this, isDarkTheme);
        mainLayout.setCenter(dashboardView.createEnhancedDashboard());
    }

    // Show goals view as right panel - make public so SidebarMenuView can access
    public void showGoalsView() {
        // Create new goals view and set it as center content
        GoalsRightView goalsView = new GoalsRightView(service, this, isDarkTheme);
        mainLayout.setCenter(goalsView.createGoalsView());
    }

    // Show statistics view as right panel - make public so SidebarMenuView can access
    public void showStatisticsView() {
        // Create new statistics view and set it as center content
        StatisticsRightView statisticsView = new StatisticsRightView(service, this, isDarkTheme);
        mainLayout.setCenter(statisticsView.createStatisticsView());
    }

    // Show tasks view - make public so SidebarMenuView can access
    public void showTasksView() {
        addTask(); // For now, redirect to add task functionality
    }

    // Show task management view - make public so SidebarMenuView can access
    public void showTaskManagementView() {
        // Create new task management view and set it as center content
        AddEditMyGoals taskManagementView = new AddEditMyGoals(service);
        taskManagementView.setDarkMode(isDarkTheme);
        mainLayout.setCenter(taskManagementView);
    }

    // Show calendar view - make public so SidebarMenuView can access
    public void showCalendarView() {
        // Create CalendarRightView and set it as center content
        CalendarRightView calendarRightView = new CalendarRightView(service, isDarkTheme);
        mainLayout.setCenter(calendarRightView);
    }

    // Make settings dialog public so SidebarMenuView can access
    public void showSettingsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Application Settings");
        alert.setContentText("Settings feature coming soon!\n\nCurrent Features:\n• Theme Toggle\n• Email Notifications\n• Statistics Dashboard");
        alert.showAndWait();
    }

    // Create main content (existing layout without title)
    private VBox createMainContent() {
        VBox layout = new VBox();
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(30);
        updateLayoutTheme(layout);

        Button createGoalButton = createButton("Create Goal", "create.png");
        Button addTaskButton = createButton("Add Task to Goal", "add.png");
        Button viewGoalsButton = createButton("View Goals and Progress", "view.png");
        Button exitButton = createButton("Exit", "exit.png");
        Button sendNotificationButton = createButton("Send Notification Now", "mark.png");
        Button viewStatisticsButton = createButton("View Statistics", "view.png");
        Button sendStatisticsButton = createButton("Send Statistics Email", "mark.png");

        // Tooltips
        createGoalButton.setTooltip(new Tooltip("Create a new goal"));
        addTaskButton.setTooltip(new Tooltip("Add a task to an existing goal"));
        viewGoalsButton.setTooltip(new Tooltip("View all goals and their progress (also available in sidebar)"));
        exitButton.setTooltip(new Tooltip("Exit the application"));
        sendNotificationButton.setTooltip(new Tooltip("Send today's tasks notification email now"));
        viewStatisticsButton.setTooltip(new Tooltip("View statistics dashboard (also available in sidebar)"));
        sendStatisticsButton.setTooltip(new Tooltip("Send statistics dashboard via email"));

        // Actions
        createGoalButton.setOnAction(e -> createGoal());
        addTaskButton.setOnAction(e -> addTask());
        viewGoalsButton.setOnAction(e -> showGoalsView()); // Updated to use new integrated view
        exitButton.setOnAction(e -> primaryStage.close());
        sendNotificationButton.setOnAction(e -> {
            try {
                MailService.sendTaskReminder("quocthien049@gmail.com", service.getTodayTasks());
                showInfoDialog("Notification Sent", "Today's tasks notification email sent.");
            } catch (Exception ex) {
                showInfoDialog("Error", "Failed to send notification: " + ex.getMessage());
            }
        });
        viewStatisticsButton.setOnAction(e -> showStatisticsView()); // Updated to use new integrated view
        sendStatisticsButton.setOnAction(e -> sendStatisticsDashboardEmail());

        VBox buttonContainer = new VBox(15);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(
            createGoalButton, addTaskButton, viewGoalsButton,
            sendNotificationButton,
            viewStatisticsButton, sendStatisticsButton, exitButton
        );

        // Bind width for responsiveness
        buttonContainer.getChildren().forEach(node -> {
            if (node instanceof Button btn) {
                btn.prefWidthProperty().bind(layout.widthProperty().multiply(0.4));
            }
        });

        StackPane cardPane = new StackPane(buttonContainer);
        cardPane.setPadding(new Insets(40, 60, 40, 60));
        cardPane.setStyle(getCardStyle());

        layout.getChildren().add(cardPane);
        return layout;
    }

    // Theme management methods
    private String getHeaderStyle() {
        return isDarkTheme ? 
            "-fx-background-color: #34495e; -fx-border-color: #2c3e50; -fx-border-width: 0 0 1 0;" :
            "-fx-background-color: #2c3e50; -fx-border-color: #34495e; -fx-border-width: 0 0 1 0;";
    }

    private String getTitleStyle() {
        return isDarkTheme ?
            "-fx-font-size: 22px; -fx-text-fill: #ecf0f1; -fx-font-weight: bold;" :
            "-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;";
    }

    private String getHeaderButtonStyle() {
        return isDarkTheme ?
            "-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 16px; -fx-cursor: hand; -fx-background-radius: 4px;" :
            "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand; -fx-background-radius: 4px;";
    }

    private String getCardStyle() {
        return isDarkTheme ?
            "-fx-background-color: #34495e; -fx-background-radius: 18px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 24, 0.2, 0, 8);" :
            "-fx-background-color: white; -fx-background-radius: 18px; -fx-effect: dropshadow(gaussian, rgba(44,62,80,0.12), 24, 0.2, 0, 8);";
    }

    private void updateLayoutTheme(VBox layout) {
        String backgroundStyle = isDarkTheme ?
            "-fx-background-color: linear-gradient(to bottom right, #2c3e50, #34495e);" :
            "-fx-background-color: linear-gradient(to bottom right, #e0eafc, #cfdef3);";
        layout.setStyle(backgroundStyle);
    }

    private void toggleTheme(Button themeButton) {
        isDarkTheme = !isDarkTheme;
        themeButton.setText(isDarkTheme ? "☀️" : "🌙");
        themeButton.getTooltip().setText(isDarkTheme ? "Switch to Light Theme" : "Switch to Dark Theme");
        
        // Update header style
        header.setStyle(getHeaderStyle());
        
        // Update header button styles
        header.getChildren().forEach(node -> {
            if (node instanceof Button btn && !btn.getText().equals("🎯")) {
                btn.setStyle(getHeaderButtonStyle());
            }
            if (node instanceof Label label && label.getText().equals("Goal & Task Manager")) {
                label.setStyle(getTitleStyle());
            }
        });
        
        // Update sidebar theme using SidebarMenuView
        if (sidebarMenuView != null) {
            sidebarMenuView.updateSidebarTheme(currentView);
        }
        
        // Update main content theme
        updateLayoutTheme(mainContent);
        
        // Update card style
        mainContent.getChildren().forEach(node -> {
            if (node instanceof StackPane stackPane) {
                stackPane.setStyle(getCardStyle());
            }
        });
    }



    private void showProfileDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Profile");
        alert.setHeaderText("Profile Information");
        alert.setContentText("Profile management coming soon!\n\nCurrent User:\n• Email: quocthien049@gmail.com\n• Theme: " + (isDarkTheme ? "Dark" : "Light"));
        alert.showAndWait();
    }

    // Button factory with consistent styling
    private Button createButton(String text, String iconName) {
        Button button = new Button(text);
        button.setStyle(baseButtonStyle());

        // Icon
        if (iconName != null && !iconName.isEmpty()) {
            try {
                Image icon = new Image(getClass().getResourceAsStream("/icons/" + iconName));
                ImageView imageView = new ImageView(icon);
                imageView.setFitWidth(20);
                imageView.setFitHeight(20);
                button.setGraphic(imageView);
            } catch (Exception ignored) {}
        }

        // Hover effects
        button.setOnMouseEntered(e -> button.setStyle(hoverButtonStyle(text)));
        button.setOnMouseExited(e -> button.setStyle(baseButtonStyle(text)));

        return button;
    }

    // Base button style
    private String baseButtonStyle() {
        return baseButtonStyle("");
    }

    private String baseButtonStyle(String text) {
        if (text.equalsIgnoreCase("Exit")) {
            return "-fx-font-size: 15px; -fx-padding: 12px 20px;" +
                   "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                   "-fx-background-radius: 8px; -fx-cursor: hand;" +
                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0.1, 0, 2);";
        }
        return "-fx-font-size: 15px; -fx-padding: 12px 20px;" +
               "-fx-background-color: #4CAF50; -fx-text-fill: white;" +
               "-fx-background-radius: 8px; -fx-cursor: hand;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0.1, 0, 2);";
    }

    private String hoverButtonStyle(String text) {
        if (text.equalsIgnoreCase("Exit")) {
            return "-fx-font-size: 15px; -fx-padding: 12px 20px;" +
                   "-fx-background-color: #c0392b; -fx-text-fill: white;" +
                   "-fx-background-radius: 8px; -fx-cursor: hand;" +
                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0.2, 0, 4);";
        }
        return "-fx-font-size: 15px; -fx-padding: 12px 20px;" +
               "-fx-background-color: #45a049; -fx-text-fill: white;" +
               "-fx-background-radius: 8px; -fx-cursor: hand;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0.2, 0, 4);";
    }

    public void createGoal() {
        GoalEditView goalEditView = new GoalEditView(service);
        goalEditView.buildScreen(primaryStage);
    }

    public void addTask() {
        AddTaskView addTaskView = new AddTaskView(service);
        addTaskView.buildScreen(primaryStage);
    }

    /**
     * @deprecated Use showGoalsView() instead for integrated panel view
     */
    @Deprecated
    public void viewGoals() {
        goalsView = new GoalsView(service);
        goalsView.buildScreen(primaryStage);
    }

    /**
     * @deprecated Use showStatisticsView() instead for integrated panel view
     */
    @Deprecated
    public void viewStatistics() {
        StatisticsView statisticsView = new StatisticsView(service);
        statisticsView.buildScreen(primaryStage);
    }

    private void sendStatisticsDashboardEmail() {
        StatisticsView statisticsView = new StatisticsView(service);
        try {
            VBox dashboard = statisticsView.createChartsContainer();
            File imageFile = exportDashboardToImage(dashboard);
            MailService.sendDashboardWithAttachment("quocthien049@gmail.com", imageFile);
            showInfoDialog("Dashboard Sent", "Statistics dashboard email sent.");
        } catch (Exception ex) {
            showInfoDialog("Error", "Failed to send dashboard: " + ex.getMessage());
        }
    }

    // Export a JavaFX node (dashboard) to a PNG image file
    private File exportDashboardToImage(VBox dashboard) throws Exception {
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        javafx.scene.image.WritableImage image = dashboard.snapshot(params, null);
        File file = File.createTempFile("statistics_dashboard", ".png");
        javax.imageio.ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        return file;
    }

    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
