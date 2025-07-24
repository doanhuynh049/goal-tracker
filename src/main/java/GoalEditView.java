import javafx.scene.Scene;
import javafx.scene.control.*;
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

    public void buildScreen(Stage primaryStage, Goal goalToEdit) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(30));

        Label titleLabel = new Label(goalToEdit == null ? "Create New Goal" : "Edit Goal");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
        mainLayout.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);

        VBox form = new VBox(18);
        form.setPadding(new Insets(30, 60, 30, 60));
        form.setStyle("-fx-background-color: #f7f7fa; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        form.setAlignment(Pos.CENTER);

        TextField titleField = new TextField(goalToEdit != null ? goalToEdit.getTitle() : "");
        titleField.setPromptText("Goal Title");
        titleField.setMaxWidth(Double.MAX_VALUE);

        ComboBox<GoalType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(GoalType.values());
        typeCombo.setValue(goalToEdit != null ? goalToEdit.getType() : GoalType.MONTHLY);
        typeCombo.setPromptText("Goal Type");
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        DatePicker targetDatePicker = new DatePicker(goalToEdit != null ? goalToEdit.getTargetDate() : LocalDate.now());
        targetDatePicker.setPromptText("Target Date");
        targetDatePicker.setMaxWidth(Double.MAX_VALUE);

        Button saveButton = new Button(goalToEdit == null ? "Create Goal" : "Save Changes");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 6px;");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setOnAction(e -> {
            String goalTitle = titleField.getText();
            GoalType goalType = typeCombo.getValue();
            LocalDate targetDate = targetDatePicker.getValue();
            if (goalTitle.isEmpty() || goalType == null || targetDate == null) {
                showInfoDialog("Missing Data", "Please fill in all required fields.");
                return;
            }
            if (goalToEdit == null) {
                service.createGoal(goalTitle, goalType, targetDate);
                showInfoDialog("Goal Created", "Goal created successfully!");
            } else {
                // For simplicity, only allow editing type and date (title is usually unique)
                // If you want to allow editing title, update the Goal class accordingly
                goalToEdit.setType(goalType);
                goalToEdit.setTargetDate(targetDate);
                showInfoDialog("Goal Updated", "Goal updated successfully!");
            }
            service.saveGoalsToFile();
        });

        form.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Type:"), typeCombo,
            new Label("Target Date:"), targetDatePicker,
            saveButton
        );

        mainLayout.setCenter(form);

        Button goBackButton = new Button("Go Back");
        Scene previousScene = primaryStage.getScene();
        goBackButton.setOnAction(e -> primaryStage.setScene(previousScene));
        HBox bottomBar = new HBox(goBackButton);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));
        mainLayout.setBottom(bottomBar);

        Scene goalEditScene = new Scene(mainLayout, 500, 400);
        primaryStage.setTitle(goalToEdit == null ? "Create Goal" : "Edit Goal");
        primaryStage.setScene(goalEditScene);
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
