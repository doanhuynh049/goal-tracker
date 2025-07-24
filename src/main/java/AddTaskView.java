import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import model.Goal;
import model.Task;
import service.GoalService;
import java.time.LocalDate;
import java.util.List;

public class AddTaskView {
    private final GoalService service;
    private final List<Goal> goals;
    private VBox mainPanel;

    public AddTaskView(GoalService service) {
        this.service = service;
        this.goals = service.getAllGoals();
    }

    public void buildScreen(Stage primaryStage) {
        Scene previousScene = primaryStage.getScene();
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(30));

        Label titleLabel = new Label("Add New Task");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
        mainLayout.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);

        VBox form = new VBox(18);
        form.setPadding(new Insets(30, 60, 30, 60));
        form.setStyle("-fx-background-color: #f7f7fa; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        form.setAlignment(Pos.CENTER);

        ComboBox<Goal> goalCombo = new ComboBox<>();
        goalCombo.getItems().addAll(goals);
        goalCombo.setPromptText("Select Goal");
        goalCombo.setCellFactory(lv -> new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        goalCombo.setButtonCell(new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });

        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");
        titleField.setMaxWidth(Double.MAX_VALUE);

        TextArea descField = new TextArea();
        descField.setPromptText("Task Description");
        descField.setPrefRowCount(3);
        descField.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Task.Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Task.Priority.values());
        priorityCombo.setValue(Task.Priority.MEDIUM);
        priorityCombo.setPromptText("Priority");
        priorityCombo.setMaxWidth(Double.MAX_VALUE);

        DatePicker dueDatePicker = new DatePicker(LocalDate.now());
        dueDatePicker.setPromptText("Due Date");
        dueDatePicker.setMaxWidth(Double.MAX_VALUE);

        Button addButton = new Button("Add Task");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 6px;");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> {
            Goal selectedGoal = goalCombo.getValue();
            String taskTitle = titleField.getText();
            String taskDesc = descField.getText();
            Task.Priority priority = priorityCombo.getValue();
            LocalDate dueDate = dueDatePicker.getValue();
            if (selectedGoal == null || taskTitle.isEmpty() || dueDate == null) {
                showInfoDialog("Missing Data", "Please fill in all required fields.");
                return;
            }
            Task task = new Task(taskTitle, taskDesc, dueDate, priority, false);
            service.addTaskToGoal(selectedGoal.getId(), task);
            showInfoDialog("Task Added", "Task added successfully!");
            // Optionally clear fields or go back
            titleField.clear();
            descField.clear();
            priorityCombo.setValue(Task.Priority.MEDIUM);
            dueDatePicker.setValue(LocalDate.now());
        });

        form.getChildren().addAll(
            new Label("Goal:"), goalCombo,
            new Label("Title:"), titleField,
            new Label("Description:"), descField,
            new Label("Priority:"), priorityCombo,
            new Label("Due Date:"), dueDatePicker,
            addButton
        );

        mainLayout.setCenter(form);

        Button goBackButton = new Button("Go Back");
        goBackButton.setOnAction(e -> primaryStage.setScene(previousScene));
        HBox bottomBar = new HBox(goBackButton);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));
        mainLayout.setBottom(bottomBar);

        Scene addTaskScene = new Scene(mainLayout, 900, 600);
        primaryStage.setTitle("Add Task");
        primaryStage.setScene(addTaskScene);
        primaryStage.show();
    }

    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
