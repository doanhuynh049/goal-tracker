import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.embed.swing.SwingFXUtils;
import model.Goal;
import model.Task;
import service.GoalService;
import util.MailService;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsView {
    private final GoalService service;
    private VBox chartsContainer;

    public StatisticsView(GoalService service) {
        this.service = service;
    }

    public void buildScreen(Stage primaryStage) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom right, #f0f4f8, #d9e2ec);");

        // Title
        Label titleLabel = new Label("Statistics Dashboard");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 20px;");
        
        // Create charts container
        chartsContainer = createChartsContainer();
        
        // Control buttons
        HBox controlButtons = createControlButtons(primaryStage);
        
        // Layout setup
        VBox topSection = new VBox(10, titleLabel, controlButtons);
        topSection.setAlignment(Pos.CENTER);
        
        ScrollPane scrollPane = new ScrollPane(chartsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        mainLayout.setTop(topSection);
        mainLayout.setCenter(scrollPane);

        Scene statisticsScene = new Scene(mainLayout, 1000, 700);
        primaryStage.setTitle("Statistics Dashboard");
        primaryStage.setScene(statisticsScene);
        primaryStage.show();
    }

    public VBox createChartsContainer() {
        VBox container = new VBox(30);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);
        
        // Task completion pie chart
        PieChart taskCompletionChart = createTaskCompletionChart();
        
        // Goals progress bar chart
        BarChart<String, Number> goalsProgressChart = createGoalsProgressChart();
        
        // Task priority distribution chart
        PieChart taskPriorityChart = createTaskPriorityChart();
        
        // Goals by type chart
        PieChart goalsByTypeChart = createGoalsByTypeChart();
        
        // Tasks completion timeline (line chart)
        LineChart<String, Number> timelineChart = createTasksTimelineChart();

        // Add charts to container with styling
        container.getChildren().addAll(
            createChartCard(taskCompletionChart, "Task Completion Overview"),
            createChartCard(goalsProgressChart, "Goals Progress"),
            createChartCard(taskPriorityChart, "Tasks by Priority"),
            createChartCard(goalsByTypeChart, "Goals by Type"),
            createChartCard(timelineChart, "Tasks Completion Timeline")
        );
        
        return container;
    }

    private VBox createChartCard(javafx.scene.Node chart, String title) {
        Label chartTitle = new Label(title);
        chartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        VBox card = new VBox(15, chartTitle, chart);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.1, 0, 4);"
        );
        card.setMaxWidth(800);
        
        return card;
    }

    private PieChart createTaskCompletionChart() {
        int completed = service.getCompletedTaskCount();
        int pending = service.getPendingTaskCount();
        
        PieChart chart = new PieChart();
        chart.getData().add(new PieChart.Data("Completed (" + completed + ")", completed));
        chart.getData().add(new PieChart.Data("Pending (" + pending + ")", pending));
        chart.setPrefSize(400, 300);
        chart.setLegendVisible(true);
        
        return chart;
    }

    private BarChart<String, Number> createGoalsProgressChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        
        xAxis.setLabel("Goals");
        yAxis.setLabel("Completion Percentage");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Progress");
        
        List<Goal> goals = service.getGoals();
        for (Goal goal : goals) {
            String goalName = goal.getName().length() > 15 ? 
                goal.getName().substring(0, 15) + "..." : goal.getName();
            series.getData().add(new XYChart.Data<>(goalName, goal.getCompletionPercentage()));
        }
        
        chart.getData().add(series);
        chart.setPrefSize(600, 300);
        chart.setLegendVisible(false);
        
        return chart;
    }

    private PieChart createTaskPriorityChart() {
        Map<Task.Priority, Long> priorityCount = service.getGoals().stream()
            .flatMap(goal -> goal.getTasks().stream())
            .filter(task -> !task.isCompleted())
            .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
            
        PieChart chart = new PieChart();
        
        for (Map.Entry<Task.Priority, Long> entry : priorityCount.entrySet()) {
            chart.getData().add(new PieChart.Data(
                entry.getKey().toString() + " (" + entry.getValue() + ")", 
                entry.getValue()
            ));
        }
        
        chart.setPrefSize(400, 300);
        chart.setLegendVisible(true);
        
        return chart;
    }

    private PieChart createGoalsByTypeChart() {
        Map<String, Long> typeCount = service.getGoals().stream()
            .collect(Collectors.groupingBy(
                goal -> goal.getType().toString(),
                Collectors.counting()
            ));
            
        PieChart chart = new PieChart();
        
        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            chart.getData().add(new PieChart.Data(
                entry.getKey() + " (" + entry.getValue() + ")", 
                entry.getValue()
            ));
        }
        
        chart.setPrefSize(400, 300);
        chart.setLegendVisible(true);
        
        return chart;
    }

    private LineChart<String, Number> createTasksTimelineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        
        xAxis.setLabel("Date");
        yAxis.setLabel("Tasks Completed");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Completed Tasks");
        
        // Get completed tasks grouped by completion date
        Map<LocalDate, Long> tasksByDate = service.getGoals().stream()
            .flatMap(goal -> goal.getTasks().stream())
            .filter(task -> task.isCompleted() && task.getCompletedDate() != null)
            .collect(Collectors.groupingBy(Task::getCompletedDate, Collectors.counting()));
        
        // Add data for the last 7 days
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            long count = tasksByDate.getOrDefault(date, 0L);
            series.getData().add(new XYChart.Data<>(dateStr, count));
        }
        
        chart.getData().add(series);
        chart.setPrefSize(600, 300);
        chart.setCreateSymbols(true);
        
        return chart;
    }

    private HBox createControlButtons(Stage primaryStage) {
        Button goBackButton = new Button("Go Back");
        goBackButton.setStyle(getButtonStyle());
        Scene previousScene = primaryStage.getScene();
        goBackButton.setOnAction(e -> primaryStage.setScene(previousScene));

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle(getButtonStyle());
        refreshButton.setOnAction(e -> refreshCharts());

        Button exportButton = new Button("Export as Image");
        exportButton.setStyle(getButtonStyle());
        exportButton.setOnAction(e -> exportDashboard());

        Button emailButton = new Button("Send via Email");
        emailButton.setStyle(getButtonStyle());
        emailButton.setOnAction(e -> sendDashboardEmail());

        HBox buttonBox = new HBox(15, goBackButton, refreshButton, exportButton, emailButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        
        return buttonBox;
    }

    private String getButtonStyle() {
        return "-fx-font-size: 14px; -fx-padding: 10px 20px;" +
               "-fx-background-color: #3498db; -fx-text-fill: white;" +
               "-fx-background-radius: 6px; -fx-cursor: hand;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0.1, 0, 1);";
    }

    private void refreshCharts() {
        chartsContainer.getChildren().clear();
        chartsContainer.getChildren().addAll(createChartsContainer().getChildren());
        showInfoDialog("Refresh", "Statistics have been refreshed.");
    }

    private void exportDashboard() {
        try {
            File imageFile = exportDashboardToImage(chartsContainer);
            showInfoDialog("Export Successful", "Dashboard exported to: " + imageFile.getAbsolutePath());
        } catch (Exception ex) {
            showInfoDialog("Export Error", "Failed to export dashboard: " + ex.getMessage());
        }
    }

    private void sendDashboardEmail() {
        try {
            File imageFile = exportDashboardToImage(chartsContainer);
            MailService.sendDashboardWithAttachment("quocthien049@gmail.com", imageFile);
            showInfoDialog("Email Sent", "Statistics dashboard email sent successfully.");
        } catch (Exception ex) {
            showInfoDialog("Email Error", "Failed to send dashboard email: " + ex.getMessage());
        }
    }

    private File exportDashboardToImage(VBox dashboard) throws Exception {
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        javafx.scene.image.WritableImage image = dashboard.snapshot(params, null);
        File file = File.createTempFile("statistics_dashboard", ".png");
        javax.imageio.ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        return file;
    }

    private void showInfoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
