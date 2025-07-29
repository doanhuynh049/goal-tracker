import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Goal;
import model.Task;
import service.GoalService;
import util.DailyScheduler;
import util.MailService;

public class MainView extends Application {
    private GoalService service = new GoalService();
    private Stage primaryStage;
    private GoalsView goalsView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
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
        Button markTaskCompletedButton = createButton("Mark Task Completed", "mark.png");
        Button saveToFileButton = createButton("Save to Excel", "save.png");
        Button exitButton = createButton("Exit", "exit.png");
        Button sendNotificationButton = createButton("Send Notification Now", "mark.png");

        // Tooltips
        createGoalButton.setTooltip(new Tooltip("Create a new goal"));
        addTaskButton.setTooltip(new Tooltip("Add a task to an existing goal"));
        viewGoalsButton.setTooltip(new Tooltip("View all goals and their progress"));
        markTaskCompletedButton.setTooltip(new Tooltip("Mark a task as completed"));
        saveToFileButton.setTooltip(new Tooltip("Save all data to Excel"));
        exitButton.setTooltip(new Tooltip("Exit the application"));
        sendNotificationButton.setTooltip(new Tooltip("Send today's tasks notification email now"));

        // Actions
        createGoalButton.setOnAction(e -> createGoal());
        addTaskButton.setOnAction(e -> addTask());
        viewGoalsButton.setOnAction(e -> viewGoals());
        markTaskCompletedButton.setOnAction(e -> markTaskCompleted());
        saveToFileButton.setOnAction(e -> saveToFile());
        exitButton.setOnAction(e -> primaryStage.close());
        sendNotificationButton.setOnAction(e -> {
            try {
                MailService.sendTaskReminder("quocthien049@gmail.com", service.getTodayTasks());
                showInfoDialog("Notification Sent", "Today's tasks notification email sent.");
            } catch (Exception ex) {
                showInfoDialog("Error", "Failed to send notification: " + ex.getMessage());
            }
        });

        VBox buttonContainer = new VBox(15);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(
            createGoalButton, addTaskButton, viewGoalsButton,
            markTaskCompletedButton, saveToFileButton, sendNotificationButton, exitButton
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
        goalEditView.buildScreen(primaryStage, null);
    }

    private void addTask() {
        AddTaskView addTaskView = new AddTaskView(service);
        addTaskView.buildScreen(primaryStage);
    }

    private void viewGoals() {
        goalsView = new GoalsView(service);
        goalsView.buildScreen(primaryStage);
    }

    private void markTaskCompleted() {
        TextInputDialog taskIdDialog = new TextInputDialog();
        taskIdDialog.setTitle("Mark Task Completed");
        taskIdDialog.setHeaderText("Enter Task ID to mark as completed:");
        taskIdDialog.showAndWait().ifPresent(taskId -> {
            service.markTaskCompleted(taskId);
            showInfoDialog("Task Completed", "Task marked as completed.");
        });
    }

    private void saveToFile() {
        service.saveGoalsToFile();
        showInfoDialog("Save Successful", "Goals and tasks saved to Excel.");
    }

    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
