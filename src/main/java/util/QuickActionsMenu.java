package util;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import model.Goal;
import model.GoalType;
import service.GoalService;

import java.time.LocalDate;
import java.util.List;

/**
 * Quick Actions Menu for common goal and task operations
 */
public class QuickActionsMenu {
    
    private final GoalService service;
    
    public QuickActionsMenu(GoalService service) {
        this.service = service;
    }
    
    /**
     * Show quick actions context menu
     */
    public void showQuickActions(Stage parentStage, double x, double y) {
        ContextMenu contextMenu = new ContextMenu();
        
        // Quick Goal Actions
        Menu quickGoalMenu = new Menu("⚡ Quick Goals");
        
        MenuItem todayGoal = new MenuItem("📅 Goal for Today");
        todayGoal.setOnAction(e -> createQuickGoal("Today's Priority", GoalType.DAILY, 1));
        
        MenuItem weekGoal = new MenuItem("📋 Weekly Goal");
        weekGoal.setOnAction(e -> createQuickGoal("This Week's Focus", GoalType.WEEKLY, 7));
        
        MenuItem monthGoal = new MenuItem("📊 Monthly Goal");
        monthGoal.setOnAction(e -> createQuickGoal("Monthly Achievement", GoalType.MONTHLY, 30));
        
        quickGoalMenu.getItems().addAll(todayGoal, weekGoal, monthGoal);
        
        // Templates Menu
        Menu templatesMenu = new Menu("📋 Templates");
        
        List<GoalTemplates.Template> featuredTemplates = GoalTemplates.getFeaturedTemplates();
        for (GoalTemplates.Template template : featuredTemplates) {
            MenuItem templateItem = new MenuItem(template.getName());
            templateItem.setOnAction(e -> createGoalFromTemplate(template));
            templatesMenu.getItems().add(templateItem);
        }
        
        MenuItem browseTemplates = new MenuItem("🔍 Browse All Templates...");
        browseTemplates.setOnAction(e -> showTemplatesBrowser(parentStage));
        templatesMenu.getItems().addAll(new SeparatorMenuItem(), browseTemplates);
        
        // Quick Task Actions
        Menu quickTaskMenu = new Menu("✅ Quick Tasks");
        
        MenuItem urgentTask = new MenuItem("🚨 Add Urgent Task");
        urgentTask.setOnAction(e -> showQuickTaskDialog("URGENT", model.Task.Priority.URGENT));
        
        MenuItem importantTask = new MenuItem("⚠️ Add Important Task");
        importantTask.setOnAction(e -> showQuickTaskDialog("IMPORTANT", model.Task.Priority.HIGH));
        
        MenuItem quickNote = new MenuItem("📝 Quick Note/Reminder");
        quickNote.setOnAction(e -> showQuickTaskDialog("REMINDER", model.Task.Priority.LOW));
        
        quickTaskMenu.getItems().addAll(urgentTask, importantTask, quickNote);
        
        // Productivity Actions
        Menu productivityMenu = new Menu("📈 Productivity");
        
        MenuItem reviewGoals = new MenuItem("🔍 Review All Goals");
        reviewGoals.setOnAction(e -> showGoalsReview(parentStage));
        
        MenuItem completeTask = new MenuItem("✅ Mark Tasks Complete");
        completeTask.setOnAction(e -> showTaskCompletion(parentStage));
        
        MenuItem updateProgress = new MenuItem("📊 Update Progress");
        updateProgress.setOnAction(e -> showProgressUpdate(parentStage));
        
        productivityMenu.getItems().addAll(reviewGoals, completeTask, updateProgress);
        
        // Add all menus to context menu
        contextMenu.getItems().addAll(
            quickGoalMenu,
            templatesMenu,
            quickTaskMenu,
            new SeparatorMenuItem(),
            productivityMenu
        );
        
        contextMenu.show(parentStage, x, y);
    }
    
    private void createQuickGoal(String defaultTitle, GoalType type, int days) {
        Goal quickGoal = new Goal(
            defaultTitle,
            "Quick goal created for immediate focus",
            type,
            LocalDate.now().plusDays(days),
            null
        );
        
        service.addGoal(quickGoal);
        showSuccessAlert("Quick Goal Created", "Goal '" + defaultTitle + "' has been created!");
    }
    
    private void createGoalFromTemplate(GoalTemplates.Template template) {
        Goal goal = template.createGoal();
        service.addGoal(goal);
        showSuccessAlert("Goal Created from Template", 
            "Goal '" + template.getName() + "' has been created with " + 
            template.getTaskTitles().size() + " tasks!");
    }
    
    private void showTemplatesBrowser(Stage parentStage) {
        Stage templatesStage = new Stage();
        templatesStage.setTitle("Goal Templates");
        templatesStage.initOwner(parentStage);
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        Text title = new Text("🎯 Choose a Goal Template");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Search box
        TextField searchBox = new TextField();
        searchBox.setPromptText("Search templates...");
        searchBox.setPrefWidth(400);
        
        // Templates list
        ListView<GoalTemplates.Template> templatesList = new ListView<>();
        templatesList.setPrefHeight(300);
        
        // Populate templates
        updateTemplatesList(templatesList, GoalTemplates.getAllTemplates());
        
        // Search functionality
        searchBox.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.trim().isEmpty()) {
                updateTemplatesList(templatesList, GoalTemplates.getAllTemplates());
            } else {
                updateTemplatesList(templatesList, GoalTemplates.searchTemplates(newText));
            }
        });
        
        // Custom cell factory for better display
        templatesList.setCellFactory(lv -> new ListCell<GoalTemplates.Template>() {
            @Override
            protected void updateItem(GoalTemplates.Template template, boolean empty) {
                super.updateItem(template, empty);
                if (empty || template == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cell = new VBox(5);
                    
                    Text nameText = new Text(template.getName());
                    nameText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    Text descText = new Text(template.getDescription());
                    descText.setStyle("-fx-font-size: 12px; -fx-fill: #666;");
                    
                    Text detailsText = new Text(
                        template.getTaskTitles().size() + " tasks • " + 
                        template.getEstimatedDaysToComplete() + " days • " +
                        template.getType().toString().replace("_", " ")
                    );
                    detailsText.setStyle("-fx-font-size: 10px; -fx-fill: #999;");
                    
                    cell.getChildren().addAll(nameText, descText, detailsText);
                    setGraphic(cell);
                    setText(null);
                }
            }
        });
        
        // Buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        Button createButton = new Button("Create Goal");
        createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        createButton.setOnAction(e -> {
            GoalTemplates.Template selected = templatesList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                createGoalFromTemplate(selected);
                templatesStage.close();
            }
        });
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> templatesStage.close());
        
        buttons.getChildren().addAll(cancelButton, createButton);
        
        layout.getChildren().addAll(title, searchBox, templatesList, buttons);
        
        Scene scene = new Scene(layout, 500, 450);
        templatesStage.setScene(scene);
        templatesStage.show();
    }
    
    private void updateTemplatesList(ListView<GoalTemplates.Template> listView, 
                                   List<GoalTemplates.Template> templates) {
        listView.getItems().clear();
        listView.getItems().addAll(templates);
    }
    
    private void showQuickTaskDialog(String taskType, model.Task.Priority priority) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add " + taskType + " Task");
        dialog.setHeaderText("Create a quick " + taskType.toLowerCase() + " task");
        dialog.setContentText("Task description:");
        
        dialog.showAndWait().ifPresent(taskDescription -> {
            if (!taskDescription.trim().isEmpty()) {
                // Find the first available goal or create a default one
                List<Goal> goals = service.getAllGoals();
                Goal targetGoal;
                
                if (goals.isEmpty()) {
                    targetGoal = new Goal(
                        "Quick Tasks",
                        "Container for quick tasks",
                        GoalType.SHORT_TERM,
                        LocalDate.now().plusDays(7),
                        null
                    );
                    service.addGoal(targetGoal);
                } else {
                    targetGoal = goals.get(0); // Use first goal
                }
                
                model.Task newTask = new model.Task(
                    taskDescription.trim(),
                    "Quick " + taskType.toLowerCase() + " task",
                    LocalDate.now().plusDays(1),
                    priority,
                    false,
                    model.Task.TaskStatus.TO_DO
                );
                
                targetGoal.addTask(newTask);
                service.saveGoalsToFile();
                
                showSuccessAlert("Task Added", 
                    taskType + " task added to goal: " + targetGoal.getTitle());
            }
        });
    }
    
    private void showGoalsReview(Stage parentStage) {
        Stage reviewStage = new Stage();
        reviewStage.setTitle("Goals Review");
        reviewStage.initOwner(parentStage);
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        Text title = new Text("📋 Goals Quick Review");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        ScrollPane scrollPane = new ScrollPane();
        VBox goalsContainer = new VBox(10);
        
        List<Goal> goals = service.getAllGoals();
        for (Goal goal : goals) {
            VBox goalCard = createGoalReviewCard(goal);
            goalsContainer.getChildren().add(goalCard);
        }
        
        scrollPane.setContent(goalsContainer);
        scrollPane.setPrefHeight(400);
        scrollPane.setFitToWidth(true);
        
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> reviewStage.close());
        
        layout.getChildren().addAll(title, scrollPane, closeButton);
        
        Scene scene = new Scene(layout, 600, 500);
        reviewStage.setScene(scene);
        reviewStage.show();
    }
    
    private VBox createGoalReviewCard(Goal goal) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.2, 0, 1);"
        );
        
        Text goalTitle = new Text(goal.getTitle());
        goalTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        ProgressBar progressBar = new ProgressBar(goal.getProgress() / 100.0);
        progressBar.setPrefWidth(200);
        
        Text progressText = new Text(String.format("%.1f%% complete", goal.getProgress()));
        progressText.setStyle("-fx-font-size: 12px; -fx-fill: #666;");
        
        Text dueDateText = new Text("Due: " + 
            (goal.getTargetDate() != null ? goal.getTargetDate().toString() : "No date"));
        dueDateText.setStyle("-fx-font-size: 11px; -fx-fill: #999;");
        
        card.getChildren().addAll(goalTitle, progressBar, progressText, dueDateText);
        return card;
    }
    
    private void showTaskCompletion(Stage parentStage) {
        // Implementation for bulk task completion
        showInfoAlert("Task Completion", "Task completion feature coming soon!");
    }
    
    private void showProgressUpdate(Stage parentStage) {
        // Implementation for progress update
        showInfoAlert("Progress Update", "Progress update feature coming soon!");
    }
    
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
