import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import model.Goal;
import model.GoalType;
import service.GoalService;

import java.time.LocalDate;
import java.util.List;

public class GoalEditView {
    private final GoalService service;
    private final List<Goal> goals;

    public GoalEditView(GoalService service) {
        this.service = service;
        this.goals = service.getAllGoals();
    }

    private Goal findGoalByTitle(String title) {
        return goals.stream()
                .filter(g -> g.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);
    }

    private void showDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateFields(String title, ComboBox<GoalType> typeCombo, DatePicker datePicker, TextArea notesArea, ProgressBar progressBar, Label progressLabel) {
        Goal matchedGoal = findGoalByTitle(title);
        if (matchedGoal != null) {
            typeCombo.setValue(matchedGoal.getType());
            datePicker.setValue(matchedGoal.getTargetDate());
            notesArea.setText(matchedGoal.getNotes());
            double progress = matchedGoal.getProgressPercent();
            progressBar.setProgress(progress);
            progressLabel.setText(String.format("%.0f%% Completed", progress * 100));
        } else {
            typeCombo.setValue(GoalType.MONTHLY);
            datePicker.setValue(LocalDate.now());
            notesArea.clear();
            progressBar.setProgress(0);
            progressLabel.setText("0% Completed");
        }
    }

    private void updateProgressBar(Goal goal, ProgressBar progressBar, Label progressLabel) {
        double progress = goal.getProgressPercent();
        progressBar.setProgress(progress);
        progressLabel.setText(String.format("%.0f%% Completed", progress * 100));
    }

    private void setupFormFields(GridPane form, ComboBox<String> titleComboBox, ComboBox<GoalType> typeCombo, DatePicker datePicker, TextArea notesArea, ProgressBar progressBar, Label progressLabel) {
        form.add(new Label("Title:"), 0, 0);
        form.add(titleComboBox, 1, 0);
        form.add(new Label("Type:"), 0, 1);
        form.add(typeCombo, 1, 1);
        form.add(new Label("Target Date:"), 0, 2);
        form.add(datePicker, 1, 2);
        form.add(new Label("Notes:"), 0, 3);
        form.add(notesArea, 1, 3);
        form.add(new Label("Progress:"), 0, 4);
        form.add(new VBox(5, progressBar, progressLabel), 1, 4);
    }

    private HBox setupActionBar(Button backBtn, Button deleteBtn, Button saveBtn) {
        HBox actionBar = new HBox(20);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        actionBar.getChildren().addAll(backBtn, deleteBtn, saveBtn);
        return actionBar;
    }

    private void setupBackground(BorderPane root) {
        BackgroundImage bgImage = new BackgroundImage(
            new Image(getClass().getResource("/background/GoalEditView_background.jpg").toExternalForm(), 900, 650, false, true),
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            BackgroundSize.DEFAULT
        );
        root.setBackground(new Background(bgImage));
    }

    public void buildScreen(Stage primaryStage, Goal selectedGoal) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(40));

        Label screenTitle = new Label("\uD83C\uDFAF Manage Goals");
        screenTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #222;");
        BorderPane.setAlignment(screenTitle, Pos.CENTER);
        root.setTop(screenTitle);

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(20);
        form.setPadding(new Insets(40));
        form.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.8);
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.3, 0, 4);
            -fx-font-size: 14px;
            -fx-font-family: 'Segoe UI', sans-serif;
        """);
        form.setAlignment(Pos.TOP_CENTER);

        ComboBox<String> titleComboBox = new ComboBox<>();
        titleComboBox.setEditable(true);
        for (Goal g : goals) {
            titleComboBox.getItems().add(g.getTitle());
        }
        titleComboBox.setPromptText("Select or type a goal title");
        titleComboBox.setMaxWidth(400);
        titleComboBox.setStyle("-fx-padding: 8;");

        ComboBox<GoalType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(GoalType.values());
        typeCombo.setValue(GoalType.MONTHLY);
        typeCombo.setMaxWidth(400);
        typeCombo.setStyle("-fx-padding: 8;");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        datePicker.setMaxWidth(400);
        datePicker.setStyle("-fx-padding: 8;");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Add more context or notes...");
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(4);
        notesArea.setMaxWidth(400);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setStyle("""
            -fx-accent: #2196f3;
            -fx-control-inner-background: #e0e0e0;
            -fx-background-insets: 0;
            -fx-background-radius: 10px;
            -fx-padding: 4;
        """);
        Label progressLabel = new Label("0% Completed");
        progressLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        titleComboBox.setOnAction(e -> updateFields(titleComboBox.getValue(), typeCombo, datePicker, notesArea, progressBar, progressLabel));
        titleComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) ->
            updateFields(newVal, typeCombo, datePicker, notesArea, progressBar, progressLabel)
        );

        Button saveBtn = new Button("Save Goal");
        saveBtn.setStyle("""
            -fx-background-color: #2196f3;
            -fx-text-fill: white;
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-background-radius: 8px;
            -fx-padding: 10 20;
        """);
        saveBtn.setOnAction(e -> {
            String title = titleComboBox.getValue().trim();
            GoalType type = typeCombo.getValue();
            LocalDate date = datePicker.getValue();
            String notes = notesArea.getText();

            if (title.isEmpty() || type == null || date == null) {
                showDialog("Incomplete", "Please fill all required fields.");
                return;
            }

            Goal goal = findGoalByTitle(title);
            if (goal == null) {
                goal = new Goal(title, type, date);
                goal.setNotes(notes);
                service.addGoal(goal);
                titleComboBox.getItems().add(title);
                showDialog("Created", "New goal created.");
            } else {
                goal.setType(type);
                goal.setTargetDate(date);
                goal.setNotes(notes);
                goal.updateLastModified();
                showDialog("Updated", "Goal updated.");
            }

            service.saveGoalsToFile();
            updateProgressBar(goal, progressBar, progressLabel);
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("""
            -fx-background-color: #f44336;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-background-radius: 8px;
        """);
        deleteBtn.setOnAction(e -> {
            String title = titleComboBox.getValue();
            Goal goal = findGoalByTitle(title);
            if (goal != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this goal?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.YES) {
                        service.deleteGoal(goal);
                        service.saveGoalsToFile();
                        titleComboBox.getItems().remove(title);
                        titleComboBox.setValue("");
                        typeCombo.setValue(GoalType.MONTHLY);
                        datePicker.setValue(LocalDate.now());
                        notesArea.clear();
                        progressBar.setProgress(0);
                        progressLabel.setText("0% Completed");
                    }
                });
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setStyle("""
            -fx-background-color: #e0e0e0;
            -fx-text-fill: black;
            -fx-font-size: 14px;
            -fx-background-radius: 8px;
        """);
        Scene previousScene = primaryStage.getScene();
        backBtn.setOnAction(e -> primaryStage.setScene(previousScene));

        HBox actionBar = setupActionBar(backBtn, deleteBtn, saveBtn);
        setupFormFields(form, titleComboBox, typeCombo, datePicker, notesArea, progressBar, progressLabel);
        VBox centerBox = new VBox(30, form, actionBar);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(20));
        setupBackground(root);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 900, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Goal Manager");
        primaryStage.show();
    }
}
