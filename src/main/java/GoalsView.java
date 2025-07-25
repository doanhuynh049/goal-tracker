import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import model.Goal;
import model.Task;
import service.GoalService;

import java.time.LocalDate;
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

        // --- UI Layout: Use two independent scroll panes for left and right panels ---
        ScrollPane leftScrollPane = new ScrollPane(leftPanel);
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setFitToHeight(true);
        leftScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        leftScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScrollPane.setPrefWidth(300);

        ScrollPane rightScrollPane = new ScrollPane(rightPanel);
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setFitToHeight(true);
        rightScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        rightScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Use an HBox to place left and right scroll panes side by side
        HBox mainContent = new HBox(leftScrollPane, rightScrollPane);
        HBox.setHgrow(rightScrollPane, Priority.ALWAYS);
        mainLayout.setLeft(null); // Remove old left
        mainLayout.setCenter(null); // Remove old center
        mainLayout.setCenter(mainContent);

        // Remove the old scrollPane wrapping mainLayout
        Scene goalsScene = new Scene(mainLayout, 900, 600);
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
        List<Task> tasks = selectedGoal.getTasks();

        // Sort: by status (In Progress, To Do, Done), then by priority (for To Do and In Progress), then by due date
        tasks.sort((task1, task2) -> {
            // 1. Status order: IN_PROGRESS < TO_DO < DONE
            int statusOrder1 = getStatusOrder(task1.getStatus());
            int statusOrder2 = getStatusOrder(task2.getStatus());
            if (statusOrder1 != statusOrder2) {
                return Integer.compare(statusOrder1, statusOrder2);
            }
            // 2. For To Do and In Progress, sort by priority (URGENT > HIGH > MEDIUM > LOW)
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
            // Task container with background color based on status
            VBox taskContainer = new VBox(10);
            taskContainer.setPadding(new Insets(14));
            String bgColor = getBackgroundColorByStatus(task.getStatus());

            taskContainer.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-border-color: #ccc; " +
                "-fx-border-radius: 10px; " +
                "-fx-background-radius: 10px;"
            );

            taskContainer.setOnMouseEntered(ev -> taskContainer.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-border-color: #aaa; " +
                "-fx-border-radius: 10px; " +
                "-fx-background-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.2, 0, 2);"
            ));
            taskContainer.setOnMouseExited(ev -> taskContainer.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                "-fx-border-color: #ccc; " +
                "-fx-border-radius: 10px; " +
                "-fx-background-radius: 10px;"
            ));

            // Task title
            Text taskTitle = new Text(task.getTitle());
            taskTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Info row: due date, completed, overdue
            String dueText = "Due: " + (task.getDueDate() != null ? task.getDueDate() : "-");
            boolean isOverdue = task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now()) && !isTaskDone(task);
            String completedText = isTaskDone(task) ? "Yes" : "No";
            String overdueText = isOverdue ? "  |  OVERDUE" : "";

            Text infoText = new Text(dueText + " | Completed: " + completedText + overdueText);
            infoText.setStyle("-fx-font-size: 12px; -fx-fill: #666;");

            // Description field
            TextArea taskDescription = new TextArea(task.getDescription());
            taskDescription.setPromptText("Add details...");
            taskDescription.setPrefRowCount(2);
            taskDescription.setWrapText(true);
            taskDescription.textProperty().addListener((obs, oldText, newText) -> {
                task.setDescription(newText);
                service.saveGoalsToFile();
            });

            // Priority dropdown
            ComboBox<Task.Priority> priorityCombo = new ComboBox<>();
            priorityCombo.getItems().addAll(Task.Priority.values());
            priorityCombo.setValue(task.getPriority());
            priorityCombo.setCellFactory(lv -> new ListCell<Task.Priority>() {
                @Override
                protected void updateItem(Task.Priority item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.toString());
                        Circle circle = new Circle(6);
                        switch (item) {
                            case URGENT: circle.setFill(javafx.scene.paint.Color.web("#e74c3c")); break;
                            case HIGH: circle.setFill(javafx.scene.paint.Color.web("#e67e22")); break;
                            case MEDIUM: circle.setFill(javafx.scene.paint.Color.web("#f1c40f")); break;
                            case LOW: circle.setFill(javafx.scene.paint.Color.web("#43e97b")); break;
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

            // Status dropdown (replaces completed checkbox)
            ComboBox<Task.TaskStatus> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll(Task.TaskStatus.values());
            statusCombo.setValue(task.getStatus());
            statusCombo.setOnAction(e -> {
                Task.TaskStatus newStatus = statusCombo.getValue();
                task.setStatus(newStatus);
                // Only set completed true if status is DONE, otherwise false
                task.setCompleted(newStatus == Task.TaskStatus.DONE);
                service.saveGoalsToFile();
                updateRightColumn(selectedGoal); // refresh UI
                updateLeftColumn(); // refresh progress bar
            });

            // Due Date
            DatePicker duePicker = new DatePicker(task.getDueDate());
            duePicker.setOnAction(e -> {
                task.setDueDate(duePicker.getValue());
                service.saveGoalsToFile();
                updateRightColumn(selectedGoal);
            });
            Button calendarBtn = new Button("\uD83D\uDCC5");
            calendarBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px;");
            calendarBtn.setOnAction(e -> duePicker.show());

            // Form layout
            GridPane form = new GridPane();
            form.setHgap(10);
            form.setVgap(10);
            form.addRow(0, new Label("Priority:"), priorityCombo);
            form.addRow(1, new Label("Status:"), statusCombo);
            form.addRow(2, new Label("Due Date:"), duePicker, calendarBtn);

            // Assemble task block
            taskContainer.getChildren().addAll(taskTitle, infoText, taskDescription, form);
            rightColumn.getChildren().add(taskContainer);
        }

        // Add Task Button
        Button addTaskButton = new Button("+");
        addTaskButton.setStyle("-fx-background-color: linear-gradient(90deg, #43e97b 0%, #38f9d7 100%); -fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-background-radius: 12px;");
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

    private String getBackgroundColorByStatus(Task.TaskStatus status) {
        switch (status) {
            case TO_DO:
                return "#f9f9f9"; // light gray
            case IN_PROGRESS:
                return "#fffde7"; // light yellow
            case DONE:
                return "#e8f5e9"; // light green
            default:
                return "#ffffff"; // fallback white
        }
    }

    // Add this method to refresh the left column's progress bars and text
    private void updateLeftColumn() {
        if (leftColumn == null) return;
        List<Goal> allGoals = service.getAllGoals();
        leftColumn.getChildren().clear();
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
    }

    // Helper for status order: IN_PROGRESS(0), TO_DO(1), DONE(2)
    private int getStatusOrder(Task.TaskStatus status) {
        if (status == Task.TaskStatus.IN_PROGRESS) return 0;
        if (status == Task.TaskStatus.TO_DO) return 1;
        return 2; // DONE
    }
    // Helper for priority order: URGENT(0), HIGH(1), MEDIUM(2), LOW(3)
    private int getPriorityOrder(Task.Priority priority) {
        switch (priority) {
            case URGENT: return 0;
            case HIGH: return 1;
            case MEDIUM: return 2;
            case LOW: return 3;
        }
        return 4;
    }
    // Helper to determine if a task is considered completed (status == DONE)
    private boolean isTaskDone(Task task) {
        return task.getStatus() == Task.TaskStatus.DONE;
    }
}
