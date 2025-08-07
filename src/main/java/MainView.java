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
    private String currentView = "dashboard"; // Track current active view

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
        BorderPane mainLayout = new BorderPane();
        
        // Create and add modern header
        header = createModernHeader();
        mainLayout.setTop(header);

        // Create and add sidebar
        sidebar = createSidebar();
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

    // Create modern sidebar navigation panel
    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle(getSidebarStyle());
        sidebar.setPrefWidth(180);
        sidebar.setMinWidth(180);

        // App Logo/Branding section
        Label sidebarBrand = new Label("Goal Tracker");
        sidebarBrand.setStyle(getSidebarBrandStyle());
        sidebarBrand.setPadding(new Insets(0, 0, 20, 0));

        // Navigation buttons
        Button dashboardBtn = createSidebarButton("🏠", "Dashboard", "dashboard");
        Button goalsBtn = createSidebarButton("🎯", "Goals", "goals");
        Button tasksBtn = createSidebarButton("📝", "Tasks", "tasks");
        Button statsBtn = createSidebarButton("📊", "Statistics", "statistics");
        Button settingsBtn = createSidebarButton("⚙️", "Settings", "settings");

        // Set initial active state
        updateSidebarButtonState(dashboardBtn, true);

        // Add all elements to sidebar
        sidebar.getChildren().addAll(
            sidebarBrand,
            new Separator(),
            dashboardBtn,
            goalsBtn,
            tasksBtn,
            statsBtn,
            new Separator(),
            settingsBtn
        );

        return sidebar;
    }

    // Create sidebar navigation button
    private Button createSidebarButton(String icon, String text, String viewName) {
        Button button = new Button(icon + "  " + text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(getSidebarButtonStyle(false));

        // Set actions for each button
        button.setOnAction(e -> {
            // Update current view and button states
            currentView = viewName;
            updateAllSidebarButtons();
            updateSidebarButtonState(button, true);
            
            // Handle navigation based on view
            switch (viewName) {
                case "dashboard":
                    showDashboard();
                    break;
                case "goals":
                    viewGoals();
                    break;
                case "tasks":
                    showTasksView();
                    break;
                case "statistics":
                    viewStatistics();
                    break;
                case "settings":
                    showSettingsDialog();
                    break;
            }
        });

        // Hover effects
        button.setOnMouseEntered(e -> {
            if (!currentView.equals(viewName)) {
                button.setStyle(getSidebarButtonHoverStyle());
            }
        });
        button.setOnMouseExited(e -> {
            if (!currentView.equals(viewName)) {
                button.setStyle(getSidebarButtonStyle(false));
            }
        });

        return button;
    }

    // Update all sidebar buttons to inactive state
    private void updateAllSidebarButtons() {
        sidebar.getChildren().forEach(node -> {
            if (node instanceof Button btn) {
                updateSidebarButtonState(btn, false);
            }
        });
    }

    // Update sidebar button state (active/inactive)
    private void updateSidebarButtonState(Button button, boolean isActive) {
        button.setStyle(getSidebarButtonStyle(isActive));
    }

    // Show dashboard (main menu)
    private void showDashboard() {
        // Return to main dashboard content
        BorderPane parent = (BorderPane) mainContent.getParent();
        parent.setCenter(createMainContent());
    }

    // Show tasks view (you can implement a dedicated tasks view later)
    private void showTasksView() {
        addTask(); // For now, redirect to add task functionality
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
        viewGoalsButton.setTooltip(new Tooltip("View all goals and their progress"));
        exitButton.setTooltip(new Tooltip("Exit the application"));
        sendNotificationButton.setTooltip(new Tooltip("Send today's tasks notification email now"));
        viewStatisticsButton.setTooltip(new Tooltip("View statistics dashboard"));
        sendStatisticsButton.setTooltip(new Tooltip("Send statistics dashboard via email"));

        // Actions
        createGoalButton.setOnAction(e -> createGoal());
        addTaskButton.setOnAction(e -> addTask());
        viewGoalsButton.setOnAction(e -> viewGoals());
        exitButton.setOnAction(e -> primaryStage.close());
        sendNotificationButton.setOnAction(e -> {
            try {
                MailService.sendTaskReminder("quocthien049@gmail.com", service.getTodayTasks());
                showInfoDialog("Notification Sent", "Today's tasks notification email sent.");
            } catch (Exception ex) {
                showInfoDialog("Error", "Failed to send notification: " + ex.getMessage());
            }
        });
        viewStatisticsButton.setOnAction(e -> viewStatistics());
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

    // Sidebar styling methods
    private String getSidebarStyle() {
        return isDarkTheme ?
            "-fx-background-color: #2c3e50; -fx-border-color: #34495e; -fx-border-width: 0 1 0 0;" :
            "-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 0 1 0 0;";
    }

    private String getSidebarBrandStyle() {
        return isDarkTheme ?
            "-fx-font-size: 16px; -fx-text-fill: #ecf0f1; -fx-font-weight: bold;" :
            "-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;";
    }

    private String getSidebarButtonStyle(boolean isActive) {
        if (isActive) {
            return isDarkTheme ?
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;" :
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;";
        } else {
            return isDarkTheme ?
                "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;" :
                "-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;";
        }
    }

    private String getSidebarButtonHoverStyle() {
        return isDarkTheme ?
            "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;" :
            "-fx-background-color: #d5dbdb; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;";
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
        
        // Update sidebar style
        sidebar.setStyle(getSidebarStyle());
        
        // Update sidebar buttons and brand
        sidebar.getChildren().forEach(node -> {
            if (node instanceof Button btn) {
                String buttonText = btn.getText();
                boolean isActive = false;
                
                // Check if this button represents the current view
                if ((buttonText.contains("Dashboard") && currentView.equals("dashboard")) ||
                    (buttonText.contains("Goals") && currentView.equals("goals")) ||
                    (buttonText.contains("Tasks") && currentView.equals("tasks")) ||
                    (buttonText.contains("Statistics") && currentView.equals("statistics")) ||
                    (buttonText.contains("Settings") && currentView.equals("settings"))) {
                    isActive = true;
                }
                
                btn.setStyle(getSidebarButtonStyle(isActive));
            }
            if (node instanceof Label label && label.getText().equals("Goal Tracker")) {
                label.setStyle(getSidebarBrandStyle());
            }
        });
        
        // Update main content theme
        updateLayoutTheme(mainContent);
        
        // Update card style
        mainContent.getChildren().forEach(node -> {
            if (node instanceof StackPane stackPane) {
                stackPane.setStyle(getCardStyle());
            }
        });
    }

    private void showSettingsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Application Settings");
        alert.setContentText("Settings feature coming soon!\n\nCurrent Features:\n• Theme Toggle\n• Email Notifications\n• Statistics Dashboard");
        alert.showAndWait();
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

    private void createGoal() {
        GoalEditView goalEditView = new GoalEditView(service);
        goalEditView.buildScreen(primaryStage);
    }

    private void addTask() {
        AddTaskView addTaskView = new AddTaskView(service);
        addTaskView.buildScreen(primaryStage);
    }

    private void viewGoals() {
        goalsView = new GoalsView(service);
        goalsView.buildScreen(primaryStage);
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

    private void viewStatistics() {
        StatisticsView statisticsView = new StatisticsView(service);
        statisticsView.buildScreen(primaryStage);
    }

    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
