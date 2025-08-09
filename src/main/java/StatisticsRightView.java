import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.embed.swing.SwingFXUtils;
import model.Goal;
import model.Task;
import service.GoalService;
import util.MailService;
import util.AppLogger;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StatisticsRightView provides an enhanced statistics interface for the Goal & Task Manager
 * to be displayed as the right panel in MainView instead of opening a new window.
 * This replaces the traditional StatisticsView popup with an integrated panel approach.
 */
public class StatisticsRightView {
    private static final java.util.logging.Logger logger = AppLogger.getLogger();
    
    private final GoalService goalService;
    private final MainView mainView;
    private final boolean isDarkTheme;
    private VBox chartsContainer;
    
    public StatisticsRightView(GoalService goalService, MainView mainView, boolean isDarkTheme) {
        this.goalService = goalService;
        this.mainView = mainView;
        this.isDarkTheme = isDarkTheme;
        logger.info("StatisticsRightView initialized");
    }
    
    /**
     * Creates and returns the complete statistics view panel
     */
    public VBox createStatisticsView() {
        logger.info("Creating StatisticsRightView panel");
        
        VBox statisticsContainer = new VBox(20);
        statisticsContainer.setPadding(new Insets(20));
        updateLayoutTheme(statisticsContainer);
        
        // Title section
        Label titleLabel = new Label("📊 Statistics Dashboard");
        titleLabel.setStyle(getTitleStyle());
        
        // Control buttons
        HBox controlButtons = createControlButtons();
        
        // Create charts container
        chartsContainer = createChartsContainer();
        
        // Create scrollable content
        ScrollPane scrollPane = new ScrollPane(chartsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox header = new VBox(10, titleLabel, controlButtons);
        header.setAlignment(Pos.CENTER);
        
        statisticsContainer.getChildren().addAll(header, new Separator(), scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        return statisticsContainer;
    }
    
    /**
     * Creates the charts container with all statistics charts
     */
    public VBox createChartsContainer() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(15));
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
    
    /**
     * Creates a styled card container for charts
     */
    private VBox createChartCard(javafx.scene.Node chart, String title) {
        Label chartTitle = new Label(title);
        chartTitle.setStyle(getChartTitleStyle());
        
        VBox card = new VBox(15, chartTitle, chart);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle(getChartCardStyle());
        card.setMaxWidth(800);
        
        return card;
    }
    
    /**
     * Creates task completion pie chart
     */
    private PieChart createTaskCompletionChart() {
        int completed = goalService.getCompletedTaskCount();
        int pending = goalService.getPendingTaskCount();
        
        PieChart chart = new PieChart();
        chart.getData().add(new PieChart.Data("Completed (" + completed + ")", completed));
        chart.getData().add(new PieChart.Data("Pending (" + pending + ")", pending));
        chart.setPrefSize(400, 300);
        chart.setLegendVisible(true);
        
        return chart;
    }
    
    /**
     * Creates goals progress bar chart
     */
    private BarChart<String, Number> createGoalsProgressChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        
        xAxis.setLabel("Goals");
        yAxis.setLabel("Completion Percentage");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Progress");
        
        List<Goal> goals = goalService.getGoals();
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
    
    /**
     * Creates task priority distribution pie chart
     */
    private PieChart createTaskPriorityChart() {
        Map<Task.Priority, Long> priorityCount = goalService.getGoals().stream()
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
    
    /**
     * Creates goals by type pie chart
     */
    private PieChart createGoalsByTypeChart() {
        Map<String, Long> typeCount = goalService.getGoals().stream()
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
    
    /**
     * Creates tasks completion timeline line chart
     */
    private LineChart<String, Number> createTasksTimelineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        
        xAxis.setLabel("Date");
        yAxis.setLabel("Tasks Completed");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Completed Tasks");
        
        // Get completed tasks grouped by completion date
        Map<LocalDate, Long> tasksByDate = goalService.getGoals().stream()
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
    
    /**
     * Creates control buttons for statistics actions
     */
    private HBox createControlButtons() {
        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle(getButtonStyle());
        refreshButton.setOnAction(e -> refreshCharts());

        Button exportButton = new Button("📷 Export Image");
        exportButton.setStyle(getButtonStyle());
        exportButton.setOnAction(e -> exportDashboard());

        Button emailButton = new Button("📧 Send Email");
        emailButton.setStyle(getButtonStyle());
        emailButton.setOnAction(e -> sendDashboardEmail());

        HBox buttonBox = new HBox(15, refreshButton, exportButton, emailButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        
        return buttonBox;
    }
    
    /**
     * Refreshes all charts with current data
     */
    private void refreshCharts() {
        chartsContainer.getChildren().clear();
        chartsContainer.getChildren().addAll(createChartsContainer().getChildren());
        showInfoMessage("Statistics have been refreshed.");
        logger.info("StatisticsRightView charts refreshed");
    }
    
    /**
     * Exports dashboard as image
     */
    private void exportDashboard() {
        try {
            File imageFile = exportDashboardToImage(chartsContainer);
            showInfoMessage("Dashboard exported to: " + imageFile.getAbsolutePath());
            logger.info("Dashboard exported as image: " + imageFile.getAbsolutePath());
        } catch (Exception ex) {
            showInfoMessage("Failed to export dashboard: " + ex.getMessage());
            logger.severe("Error exporting dashboard: " + ex.getMessage());
        }
    }
    
    /**
     * Sends dashboard via email
     */
    private void sendDashboardEmail() {
        try {
            File imageFile = exportDashboardToImage(chartsContainer);
            MailService.sendDashboardWithAttachment("quocthien049@gmail.com", imageFile);
            showInfoMessage("Statistics dashboard email sent successfully.");
            logger.info("Dashboard email sent to: quocthien049@gmail.com");
        } catch (Exception ex) {
            showInfoMessage("Failed to send dashboard email: " + ex.getMessage());
            logger.severe("Error sending dashboard email: " + ex.getMessage());
        }
    }
    
    /**
     * Exports dashboard to image file
     */
    private File exportDashboardToImage(VBox dashboard) throws Exception {
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        javafx.scene.image.WritableImage image = dashboard.snapshot(params, null);
        File file = File.createTempFile("statistics_dashboard", ".png");
        javax.imageio.ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        return file;
    }
    
    /**
     * Shows an information message to the user
     */
    private void showInfoMessage(String message) {
        // For now, log the message. In future could show toast/snackbar
        logger.info("User notification: " + message);
        
        // Also show alert dialog for now
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Styling methods
    private void updateLayoutTheme(VBox layout) {
        String backgroundStyle = isDarkTheme ?
            "-fx-background-color: linear-gradient(to bottom right, #2c3e50, #34495e);" :
            "-fx-background-color: linear-gradient(to bottom right, #e0eafc, #cfdef3);";
        layout.setStyle(backgroundStyle);
    }
    
    private String getTitleStyle() {
        return isDarkTheme ?
            "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;" :
            "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;";
    }
    
    private String getButtonStyle() {
        return isDarkTheme ?
            "-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 6px; -fx-cursor: hand;" :
            "-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 6px; -fx-cursor: hand;";
    }
    
    private String getChartTitleStyle() {
        return isDarkTheme ?
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;" :
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;";
    }
    
    private String getChartCardStyle() {
        return isDarkTheme ?
            "-fx-background-color: #34495e; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.1, 0, 4);" :
            "-fx-background-color: white; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.1, 0, 4);";
    }
}
