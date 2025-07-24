import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import model.Goal;
import model.Task;
import service.GoalService;
import java.util.List;

public class GoalsView {
    private final GoalService service;
    private VBox leftColumn;
    private VBox rightColumn;

    public GoalsView(GoalService service) {
        this.service = service;
    }

    public void buildScreen(Stage primaryStage) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));

        VBox leftColumn = createLeftColumn();
        VBox rightColumn = createRightColumn();

        // Show tasks of the first goal if available
        java.util.List<Goal> allGoals = service.getAllGoals();
        if (!allGoals.isEmpty()) {
            updateRightColumn(allGoals.get(0));
        }

        Button goBackButton = new Button("Go Back");
        goBackButton.setMaxWidth(Double.MAX_VALUE);
        Scene previousScene = primaryStage.getScene();
        goBackButton.setOnAction(e -> primaryStage.setScene(previousScene));

        VBox leftPanel = new VBox(10);
        leftPanel.getChildren().addAll(goBackButton, leftColumn);
        VBox.setVgrow(leftColumn, Priority.ALWAYS);

        // Save to Excel button at bottom right
        Button saveButton = new Button("Save to Excel");
        saveButton.setOnAction(e -> service.saveGoalsToFile());
        HBox saveButtonBox = new HBox(saveButton);
        saveButtonBox.setPadding(new Insets(10));
        saveButtonBox.setStyle("-fx-alignment: bottom-right;");

        // Remove floating button and rightStack, use rightPanel as before
        BorderPane rightPanel = new BorderPane();
        rightPanel.setCenter(rightColumn);
        rightPanel.setBottom(saveButtonBox);
        mainLayout.setLeft(leftPanel);
        mainLayout.setCenter(rightPanel);

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene goalsScene = new Scene(scrollPane, 800, 600);
        primaryStage.setTitle("Goals and Tasks");
        primaryStage.setScene(goalsScene);
        primaryStage.show();
    }

    public VBox createLeftColumn() {
        leftColumn = new VBox(10);
        leftColumn.setPadding(new Insets(10));
        ScrollPane leftScroll = new ScrollPane(leftColumn);
        leftScroll.setFitToHeight(true);
        leftScroll.setFitToWidth(true);
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        List<Goal> allGoals = service.getAllGoals();
        for (int i = 0; i < Math.min(allGoals.size(), 10); i++) {
            Goal goal = allGoals.get(i);
            VBox goalContainer = new VBox(5);
            goalContainer.setPadding(new Insets(10));
            goalContainer.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");
            Text goalTitleText = new Text(goal.getTitle());
            goalTitleText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            ProgressBar progressBar = new ProgressBar(goal.getProgress() / 100.0);
            progressBar.setPrefWidth(200);
            Text progressText = new Text(String.format("%.2f%%", goal.getProgress()));
            progressText.setStyle("-fx-font-size: 12px;");
            goalContainer.getChildren().addAll(goalTitleText, progressBar, progressText);
            goalContainer.setOnMouseClicked(event -> updateRightColumn(goal));
            leftColumn.getChildren().add(goalContainer);
        }
        return leftColumn;
    }

    public VBox createRightColumn() {
        rightColumn = new VBox(10);
        rightColumn.setPadding(new Insets(10));
        ScrollPane rightScroll = new ScrollPane(rightColumn);
        rightScroll.setFitToHeight(true);
        rightScroll.setFitToWidth(true);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        return rightColumn;
    }

    public void updateRightColumn(Goal selectedGoal) {
        if (rightColumn == null) return;
        rightColumn.getChildren().clear();
        VBox[] taskContainers = new VBox[10];
        for (int i = 0; i < 10; i++) {
            VBox taskContainer = new VBox(5);
            taskContainer.setPadding(new Insets(10));
            rightColumn.getChildren().add(taskContainer);
            taskContainers[i] = taskContainer;
        }
        List<Task> tasks = selectedGoal.getTasks();
        if (tasks.isEmpty()) {
            Label emptyLabel = new Label("No tasks for this goal.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");
            VBox emptyBox = new VBox(emptyLabel);
            emptyBox.setPadding(new Insets(20));
            emptyBox.setStyle("-fx-alignment: center;");
            rightColumn.getChildren().clear();
            rightColumn.getChildren().add(emptyBox);
            return;
        }
        tasks.sort((task1, task2) -> {
            if (task1.isCompleted() != task2.isCompleted()) {
                return task1.isCompleted() ? 1 : -1;
            }
            return task2.getDueDate().compareTo(task1.getDueDate());
        });
        for (int i = 0; i < Math.min(tasks.size(), 10); i++) {
            Task task = tasks.get(i);
            VBox taskContainer = taskContainers[i];
            if (task.isCompleted()) {
                taskContainer.setStyle("-fx-border-color: #eee; -fx-border-radius: 5px; -fx-background-color: #d4edda;");
            } else {
                taskContainer.setStyle("-fx-border-color: #eee; -fx-border-radius: 5px; -fx-background-color: #fff3cd;");
            }
            Text taskTitle = new Text(task.getTitle());
            taskTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: normal;");
            Text taskInfoText = new Text("Due: " + task.getDueDate() +
                " | Completed: " + (task.isCompleted() ? "Yes" : "No") +
                (task.isCompleted() && task.getCompletedDate() != null ?
                    " on " + task.getCompletedDate() : ""));
            taskInfoText.setStyle("-fx-font-size: 12px; -fx-font-style: italic;");
            if (task.isCompleted()) {
                // Only show info and a button to mark as undone
                Button markUndoneButton = new Button("Mark as Undone");
                markUndoneButton.setOnAction(e -> {
                    task.setCompleted(false);
                    service.saveGoalsToFile();
                    updateRightColumn(selectedGoal);
                });
                HBox controls = new HBox(10, markUndoneButton);
                controls.setPadding(new Insets(5, 0, 0, 0));
                taskContainer.getChildren().addAll(taskTitle, taskInfoText, controls);
            } else {
                // Priority ComboBox
                ComboBox<Task.Priority> priorityCombo = new ComboBox<>();
                priorityCombo.getItems().addAll(Task.Priority.values());
                priorityCombo.setValue(task.getPriority());
                priorityCombo.setOnAction(e -> {
                    task.setPriority(priorityCombo.getValue());
                    service.saveGoalsToFile();
                });
                // Status CheckBox
                CheckBox completedCheck = new CheckBox("Completed");
                completedCheck.setSelected(task.isCompleted());
                completedCheck.setOnAction(e -> {
                    task.setCompleted(completedCheck.isSelected());
                    service.saveGoalsToFile();
                    updateRightColumn(selectedGoal);
                });
                // Due Date Picker
                DatePicker dueDatePicker = new DatePicker(task.getDueDate());
                dueDatePicker.setOnAction(e -> {
                    task.setDueDate(dueDatePicker.getValue());
                    service.saveGoalsToFile();
                    updateRightColumn(selectedGoal);
                });
                HBox controls = new HBox(10, new Label("Priority:"), priorityCombo, completedCheck, new Label("Due Date:"), dueDatePicker);
                controls.setPadding(new Insets(5, 0, 0, 0));
                taskContainer.getChildren().addAll(taskTitle, taskInfoText, controls);
            }
        }
        // Add "+" button as a full-width item right after the last task item
        Button addTaskButton = new Button("+");
        addTaskButton.setStyle("-fx-background-color: linear-gradient(90deg, #43e97b 0%, #38f9d7 100%); -fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-background-radius: 12px; -fx-min-height: 60px; -fx-cursor: hand; margin-top: 16px; box-shadow: 0 4px 16px rgba(67,233,123,0.15);");
        addTaskButton.setMaxWidth(Double.MAX_VALUE);
        addTaskButton.setPrefHeight(60);
        addTaskButton.setOnAction(e -> {
            AddTaskView addTaskView = new AddTaskView(service);
            addTaskView.buildScreen((Stage) addTaskButton.getScene().getWindow());
        });
        rightColumn.getChildren().add(addTaskButton);
    }

    public VBox getLeftColumn() {
        return leftColumn;
    }
    public VBox getRightColumn() {
        return rightColumn;
    }
}
