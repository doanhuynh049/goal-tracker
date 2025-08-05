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

        VBox layout = new VBox();
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(30);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #e0eafc, #cfdef3);");

        Label titleLabel = new Label("Goal and Task Management");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button createGoalButton = createButton("Create Goal", "create.png");
        Button addTaskButton = createButton("Add Task to Goal", "add.png");
        Button viewGoalsButton = createButton("View Goals and Progress", "view.png");
        Button exitButton = createButton("Exit", "exit.png");
        Button sendNotificationButton = createButton("Send Notification Now", "mark.png");
        Button viewStatisticsButton = createButton("View Statistics", "stats.png");
        Button sendStatisticsButton = createButton("Send Statistics Email", "email.png");

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

        VBox cardContent = new VBox(25);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.getChildren().addAll(titleLabel, buttonContainer);

        StackPane cardPane = new StackPane(cardContent);
        cardPane.setPadding(new Insets(40, 60, 40, 60));
        cardPane.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18px;" +
            "-fx-effect: dropshadow(gaussian, rgba(44,62,80,0.12), 24, 0.2, 0, 8);"
        );

        layout.getChildren().add(cardPane);

        Scene mainScene = new Scene(layout, 900, 600);
        primaryStage.setTitle("Goal and Task Management");
        primaryStage.setScene(mainScene);
        primaryStage.show();
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
