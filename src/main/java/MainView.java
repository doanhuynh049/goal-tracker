import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import model.Goal;
import model.GoalType;
import model.Task;
import service.GoalService;
import java.time.LocalDate;

public class MainView extends Application {
    private GoalService service = new GoalService();
    private Stage primaryStage;
    private GoalsView goalsView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        service.loadGoalsFromFile();

        VBox layout = new VBox(10);
        Scene mainScene = new Scene(layout, 600, 400);

        Label label = new Label("Goal and Task Management");

        Button createGoalButton = new Button("Create Goal");
        Button addTaskButton = new Button("Add Task to Goal");
        Button viewGoalsButton = new Button("View Goals and Progress");
        Button markTaskCompletedButton = new Button("Mark Task Completed");
        Button saveToFileButton = new Button("Save to Excel");
        Button exitButton = new Button("Exit");

        layout.getChildren().addAll(
            label,
            createGoalButton,
            addTaskButton,
            viewGoalsButton,
            markTaskCompletedButton,
            saveToFileButton,
            exitButton
        );

        createGoalButton.setOnAction(e -> {
            GoalEditView goalEditView = new GoalEditView(service);
            goalEditView.buildScreen(primaryStage, null);
        });
        addTaskButton.setOnAction(e -> addTask());
        viewGoalsButton.setOnAction(e -> viewGoals());
        markTaskCompletedButton.setOnAction(e -> markTaskCompleted());
        saveToFileButton.setOnAction(e -> saveToFile());
        exitButton.setOnAction(e -> primaryStage.close());

        primaryStage.setTitle("Goal and Task Management");
        primaryStage.setScene(mainScene);
        primaryStage.show();
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
