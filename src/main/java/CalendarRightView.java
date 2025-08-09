import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import model.Task;
import model.Goal;
import service.GoalService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CalendarRightView provides a Google Calendar-style interface for scheduling and managing tasks.
 * This panel offers a monthly calendar grid view with Google Calendar sync capabilities.
 */
public class CalendarRightView extends VBox {
    private final GoalService goalService;
    private final boolean isDarkTheme;

    // Calendar state
    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private GridPane calendarGrid;
    private Label monthYearLabel;
    
    // UI controls
    private Button prevMonthBtn;
    private Button nextMonthBtn;
    private Button todayBtn;
    private Button syncBtn;
    private VBox taskSidebar;
    private ScrollPane taskScrollPane;
    
    // Google Calendar integration
    private WebView webView;
    private boolean isGoogleCalendarMode = false; // Start with local calendar first

    public CalendarRightView(GoalService goalService, boolean isDarkTheme) {
        this.goalService = goalService;
        this.isDarkTheme = isDarkTheme;
        this.currentYearMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();
        initializeCalendar();
    }

    private void initializeCalendar() {
        setSpacing(0);
        setPadding(new Insets(0));
        setStyle(getBackgroundStyle());
        
        // Create main container with Google Calendar-style layout
        VBox mainContainer = new VBox(0);
        
        // Header with navigation and controls
        HBox header = createCalendarHeader();
        
        // Day labels (Sun, Mon, Tue, etc.) - Google Calendar style
        HBox dayLabels = createDayLabels();
        
        // Calendar grid with Google Calendar-style layout
        calendarGrid = new GridPane();
        calendarGrid.setStyle(getCalendarGridStyle());
        calendarGrid.setGridLinesVisible(true);
        calendarGrid.setPrefSize(800, 600);
        
        // Make calendar cells expand to fill available space
        for (int col = 0; col < 7; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / 7);
            colConstraints.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(colConstraints);
        }
        
        for (int row = 0; row < 6; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / 6);
            rowConstraints.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(rowConstraints);
        }
        
        ScrollPane calendarScrollPane = new ScrollPane(calendarGrid);
        calendarScrollPane.setFitToWidth(true);
        calendarScrollPane.setFitToHeight(true);
        calendarScrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(calendarScrollPane, Priority.ALWAYS);
        
        mainContainer.getChildren().addAll(header, dayLabels, calendarScrollPane);
        getChildren().add(mainContainer);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);
        
        // Start with local Google Calendar-style view
        updateCalendarDisplay();
    }
    
    private VBox createCalendarContainer() {
        VBox container = new VBox(0);
        container.setStyle(getCalendarContainerStyle());
        
        // Header with navigation and controls
        HBox header = createCalendarHeader();
        
        // Day labels (Mon, Tue, Wed, etc.)
        HBox dayLabels = createDayLabels();
        
        // Calendar grid
        calendarGrid = new GridPane();
        calendarGrid.setStyle(getCalendarGridStyle());
        ScrollPane calendarScrollPane = new ScrollPane(calendarGrid);
        calendarScrollPane.setFitToWidth(true);
        calendarScrollPane.setFitToHeight(true);
        calendarScrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(calendarScrollPane, Priority.ALWAYS);
        
        container.getChildren().addAll(header, dayLabels, calendarScrollPane);
        return container;
    }
    
    private HBox createCalendarHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(getHeaderStyle());
        
        // Navigation buttons
        prevMonthBtn = new Button("❮");
        prevMonthBtn.setStyle(getNavButtonStyle());
        prevMonthBtn.setOnAction(e -> navigateMonth(-1));
        
        nextMonthBtn = new Button("❯");
        nextMonthBtn.setStyle(getNavButtonStyle());
        nextMonthBtn.setOnAction(e -> navigateMonth(1));
        
        // Month/Year label
        monthYearLabel = new Label();
        monthYearLabel.setStyle(getMonthYearLabelStyle());
        updateMonthYearLabel();
        
        // Today button
        todayBtn = new Button("Today");
        todayBtn.setStyle(getTodayButtonStyle());
        todayBtn.setOnAction(e -> goToToday());
        
        // Google Calendar sync button
        syncBtn = new Button(isGoogleCalendarMode ? "📅 Show Local" : "🌐 Sync with Google");
        syncBtn.setStyle(getSyncButtonStyle());
        syncBtn.setOnAction(e -> toggleGoogleCalendar());
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(prevMonthBtn, nextMonthBtn, monthYearLabel, spacer, todayBtn, syncBtn);
        return header;
    }
    
    private HBox createDayLabels() {
        HBox dayLabels = new HBox(0);
        dayLabels.setStyle(getDayLabelsStyle());
        
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            Label dayLabel = new Label(day);
            dayLabel.setStyle(getDayLabelStyle());
            dayLabel.setPrefWidth(Double.MAX_VALUE);
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            HBox.setHgrow(dayLabel, Priority.ALWAYS);
            dayLabels.getChildren().add(dayLabel);
        }
        
        return dayLabels;
    }
    
    private VBox createTaskSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle(getTaskSidebarStyle());
        
        // Sidebar header
        HBox sidebarHeader = new HBox(10);
        sidebarHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label sidebarTitle = new Label("📅 " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        sidebarTitle.setStyle(getSidebarTitleStyle());
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Button addTaskBtn = new Button("➕");
        addTaskBtn.setStyle(getAddTaskButtonStyle());
        addTaskBtn.setOnAction(e -> showAddTaskDialog());
        
        sidebarHeader.getChildren().addAll(sidebarTitle, headerSpacer, addTaskBtn);
        
        // Task list
        taskScrollPane = new ScrollPane();
        taskScrollPane.setFitToWidth(true);
        taskScrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(taskScrollPane, Priority.ALWAYS);
        
        sidebar.getChildren().addAll(sidebarHeader, new Separator(), taskScrollPane);
        return sidebar;
    }
    
    private void updateCalendarDisplay() {
        if (isGoogleCalendarMode) {
            showGoogleCalendarWeb();
            return;
        }
        
        calendarGrid.getChildren().clear();
        
        // Get first day of month and calculate starting position
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0, Monday = 1, etc.
        
        // Previous month's trailing days
        YearMonth prevMonth = currentYearMonth.minusMonths(1);
        int daysInPrevMonth = prevMonth.lengthOfMonth();
        
        int dayCounter = 1;
        int nextMonthDay = 1;
        
        // Create 6 rows for the calendar (max needed for any month)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                VBox dayCell = createDayCell(row, col, startDayOfWeek, dayCounter, daysInPrevMonth, nextMonthDay);
                calendarGrid.add(dayCell, col, row);
                
                // Update counters
                int cellIndex = row * 7 + col;
                if (cellIndex < startDayOfWeek) {
                    // Previous month days - already handled in createDayCell
                } else if (cellIndex < startDayOfWeek + currentYearMonth.lengthOfMonth()) {
                    dayCounter++;
                } else {
                    nextMonthDay++;
                }
            }
        }
        
        updateMonthYearLabel();
    }
    
    private VBox createDayCell(int row, int col, int startDayOfWeek, int dayCounter, int daysInPrevMonth, int nextMonthDay) {
        VBox cell = new VBox(4);
        cell.setPrefSize(130, 110);
        cell.setMinSize(120, 100);
        cell.setPadding(new Insets(8));
        cell.setAlignment(Pos.TOP_LEFT);
        
        int cellIndex = row * 7 + col;
        LocalDate cellDate;
        boolean isCurrentMonth = true;
        
        // Determine the date for this cell
        if (cellIndex < startDayOfWeek) {
            // Previous month
            int prevMonthDay = daysInPrevMonth - (startDayOfWeek - cellIndex - 1);
            cellDate = currentYearMonth.minusMonths(1).atDay(prevMonthDay);
            isCurrentMonth = false;
        } else if (cellIndex < startDayOfWeek + currentYearMonth.lengthOfMonth()) {
            // Current month
            cellDate = currentYearMonth.atDay(dayCounter);
        } else {
            // Next month
            cellDate = currentYearMonth.plusMonths(1).atDay(nextMonthDay);
            isCurrentMonth = false;
        }
        
        // Day number with Google Calendar styling
        HBox dayNumberContainer = new HBox();
        dayNumberContainer.setAlignment(Pos.TOP_LEFT);
        
        Label dayNumber = new Label(String.valueOf(cellDate.getDayOfMonth()));
        dayNumber.setStyle(getDayNumberStyle(cellDate, isCurrentMonth));
        
        // For today, add a blue circle background
        if (cellDate.equals(LocalDate.now())) {
            dayNumber.setStyle(getDayNumberStyle(cellDate, isCurrentMonth) + 
                             "; -fx-background-color: #1a73e8; -fx-background-radius: 20; -fx-padding: 8; -fx-min-width: 32; -fx-min-height: 32; -fx-alignment: center;");
        }
        
        dayNumberContainer.getChildren().add(dayNumber);
        
        // Tasks for this day with Google Calendar-style event bars
        VBox tasksContainer = new VBox(2);
        tasksContainer.setMaxHeight(70);
        
        List<Task> dayTasks = getTasksForDate(cellDate);
        for (Task task : dayTasks.stream().limit(3).collect(Collectors.toList())) {
            Label taskLabel = new Label(task.getTitle());
            taskLabel.setStyle(getTaskLabelStyle(task));
            taskLabel.setMaxWidth(Double.MAX_VALUE);
            taskLabel.setWrapText(false);
            taskLabel.setEllipsisString("...");
            tasksContainer.getChildren().add(taskLabel);
        }
        
        if (dayTasks.size() > 3) {
            Label moreLabel = new Label("+" + (dayTasks.size() - 3) + " more");
            moreLabel.setStyle(getMoreTasksLabelStyle());
            tasksContainer.getChildren().add(moreLabel);
        }
        
        cell.getChildren().addAll(dayNumberContainer, tasksContainer);
        cell.setStyle(getDayCellStyle(cellDate, isCurrentMonth));
        
        // Click handler
        final LocalDate finalCellDate = cellDate;
        final boolean finalIsCurrentMonth = isCurrentMonth;
        cell.setOnMouseClicked(e -> {
            selectedDate = finalCellDate;
            updateCalendarDisplay(); // Refresh to show selection
        });
        
        // Hover effect
        cell.setOnMouseEntered(e -> cell.setStyle(getDayCellHoverStyle(finalCellDate, finalIsCurrentMonth)));
        cell.setOnMouseExited(e -> cell.setStyle(getDayCellStyle(finalCellDate, finalIsCurrentMonth)));
        
        return cell;
    }
    
    // Navigation methods
    private void navigateMonth(int direction) {
        currentYearMonth = currentYearMonth.plusMonths(direction);
        updateCalendarDisplay();
    }
    
    private void goToToday() {
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        updateCalendarDisplay();
        updateTaskSidebar();
    }
    
    private void updateMonthYearLabel() {
        monthYearLabel.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentYearMonth.getYear());
    }
    
    // Google Calendar Integration
    private void toggleGoogleCalendar() {
        isGoogleCalendarMode = !isGoogleCalendarMode;
        syncBtn.setText(isGoogleCalendarMode ? "📅 Show Local" : "🌐 Sync with Google");
        
        if (isGoogleCalendarMode) {
            showGoogleCalendarWeb();
        } else {
            updateCalendarDisplay();
        }
    }
    
    private void showGoogleCalendarWeb() {
        calendarGrid.getChildren().clear();
        
        try {
            if (webView == null) {
                webView = new WebView();
                webView.getEngine().load("https://calendar.google.com/calendar/embed?src=primary&ctz=America%2FNew_York");
            }
            
            // Replace calendar grid with web view
            ScrollPane webScrollPane = new ScrollPane(webView);
            webScrollPane.setFitToWidth(true);
            webScrollPane.setFitToHeight(true);
            webScrollPane.setStyle("-fx-background-color: transparent;");
            
            // Add web view to calendar grid area
            calendarGrid.add(webScrollPane, 0, 0, 7, 6);
            GridPane.setHgrow(webScrollPane, Priority.ALWAYS);
            GridPane.setVgrow(webScrollPane, Priority.ALWAYS);
        } catch (Exception e) {
            // Fallback if WebView is not available
            showWebViewNotAvailableMessage();
        }
    }
    
    private void showWebViewNotAvailableMessage() {
        VBox messageContainer = new VBox(20);
        messageContainer.setAlignment(Pos.CENTER);
        messageContainer.setPadding(new Insets(50));
        messageContainer.setStyle("-fx-background-color: " + (isDarkTheme ? "#34495e" : "#f8f9fa") + ";");
        
        Label titleLabel = new Label("🌐 Google Calendar Integration");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + 
                           (isDarkTheme ? "#ecf0f1" : "#2c3e50") + ";");
        
        Label messageLabel = new Label("WebView is not available in this JavaFX distribution.\nPlease use the local calendar view or access Google Calendar directly in your browser.");
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + 
                             (isDarkTheme ? "#bdc3c7" : "#7f8c8d") + "; -fx-text-alignment: center;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        
        Button openBrowserBtn = new Button("🌍 Open Google Calendar in Browser");
        openBrowserBtn.setStyle(getSyncButtonStyle());
        openBrowserBtn.setOnAction(e -> openGoogleCalendarInBrowser());
        
        Button backToLocalBtn = new Button("📅 Back to Local Calendar");
        backToLocalBtn.setStyle(getTodayButtonStyle());
        backToLocalBtn.setOnAction(e -> {
            isGoogleCalendarMode = false;
            syncBtn.setText("🌐 Sync with Google");
            updateCalendarDisplay();
        });
        
        messageContainer.getChildren().addAll(titleLabel, messageLabel, openBrowserBtn, backToLocalBtn);
        
        calendarGrid.add(messageContainer, 0, 0, 7, 6);
        GridPane.setHgrow(messageContainer, Priority.ALWAYS);
        GridPane.setVgrow(messageContainer, Priority.ALWAYS);
    }
    
    private void openGoogleCalendarInBrowser() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String url = "https://calendar.google.com";
            
            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec("xdg-open " + url);
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Open Google Calendar");
            alert.setHeaderText("Manual Browser Launch Required");
            alert.setContentText("Please manually open your browser and navigate to:\nhttps://calendar.google.com");
            alert.showAndWait();
        }
    }
    
    // Task Management
    private List<Task> getTasksForDate(LocalDate date) {
        List<Task> tasksForDate = new ArrayList<>();
        
        for (Goal goal : goalService.getAllGoals()) {
            for (Task task : goal.getTasks()) {
                if (task.getDueDate() != null && task.getDueDate().equals(date)) {
                    tasksForDate.add(task);
                }
            }
        }
        
        return tasksForDate;
    }
    
    private void updateTaskSidebar() {
        VBox taskList = new VBox(8);
        List<Task> dayTasks = getTasksForDate(selectedDate);
        
        // Update sidebar title
        HBox sidebarHeader = (HBox) taskSidebar.getChildren().get(0);
        Label sidebarTitle = (Label) sidebarHeader.getChildren().get(0);
        sidebarTitle.setText("📅 " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + " (" + dayTasks.size() + " tasks)");
        
        if (dayTasks.isEmpty()) {
            Label noTasksLabel = new Label("No tasks for this day");
            noTasksLabel.setStyle(getNoTasksLabelStyle());
            taskList.getChildren().add(noTasksLabel);
        } else {
            for (Task task : dayTasks) {
                VBox taskCard = createTaskCard(task);
                taskList.getChildren().add(taskCard);
            }
        }
        
        taskScrollPane.setContent(taskList);
    }
    
    private VBox createTaskCard(Task task) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle(getTaskCardStyle(task));
        
        // Task title with priority indicator
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label priorityIcon = new Label(getPriorityIcon(task.getPriority()));
        priorityIcon.setStyle("-fx-font-size: 14px;");
        
        Label statusIcon = new Label(getStatusIcon(task.getStatus()));
        statusIcon.setStyle("-fx-font-size: 14px;");
        
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle(getTaskTitleStyle(task));
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(200);
        
        titleRow.getChildren().addAll(priorityIcon, statusIcon, titleLabel);
        
        // Task description (if available)
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            Label descLabel = new Label(task.getDescription());
            descLabel.setStyle(getTaskDescriptionStyle());
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(250);
            card.getChildren().add(descLabel);
        }
        
        card.getChildren().add(titleRow);
        
        // Click handler to edit task
        card.setOnMouseClicked(e -> showEditTaskDialog(task));
        
        return card;
    }
    
    private void updateCalendarHighlight() {
        updateCalendarDisplay(); // Refresh to show current selection
    }
    
    private void showAddTaskDialog() {
        showTaskDialog(null);
    }
    
    private void showEditTaskDialog(Task task) {
        showTaskDialog(task);
    }
    
    private void showTaskDialog(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(task == null ? "Add New Task" : "Edit Task");
        dialog.setHeaderText(task == null ? "Create a new task for " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "Edit task details");
        
        // Dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Task title");
        if (task != null) titleField.setText(task.getTitle());
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Task description (optional)");
        descArea.setPrefRowCount(3);
        if (task != null && task.getDescription() != null) descArea.setText(task.getDescription());
        
        ComboBox<Task.Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Task.Priority.values());
        priorityCombo.setValue(task != null ? task.getPriority() : Task.Priority.MEDIUM);
        
        ComboBox<Task.TaskStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(Task.TaskStatus.values());
        statusCombo.setValue(task != null ? task.getStatus() : Task.TaskStatus.TO_DO);
        
        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setValue(task != null ? task.getDueDate() : selectedDate);
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(priorityCombo, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusCombo, 1, 3);
        grid.add(new Label("Due Date:"), 0, 4);
        grid.add(dueDatePicker, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        if (task == null) {
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        } else {
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, cancelButtonType);
        }
        
        // Enable save button only when title is provided
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });
        
        // Delete button handler
        if (task != null) {
            Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
            deleteButton.setOnAction(e -> {
                deleteTask(task);
                dialog.close();
            });
        }
        
        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (task == null) {
                    // Create new task
                    Task newTask = new Task(titleField.getText().trim(), descArea.getText(), dueDatePicker.getValue());
                    newTask.setPriority(priorityCombo.getValue());
                    newTask.setStatus(statusCombo.getValue());
                    
                    // Add to first available goal
                    List<Goal> goals = goalService.getAllGoals();
                    if (!goals.isEmpty()) {
                        goals.get(0).addTask(newTask);
                        goalService.saveGoalsToFile();
                    }
                    return newTask;
                } else {
                    // Update existing task
                    task.setTitle(titleField.getText().trim());
                    task.setDescription(descArea.getText());
                    task.setPriority(priorityCombo.getValue());
                    task.setStatus(statusCombo.getValue());
                    task.setDueDate(dueDatePicker.getValue());
                    goalService.saveGoalsToFile();
                    return task;
                }
            }
            return null;
        });
        
        Optional<Task> result = dialog.showAndWait();
        if (result.isPresent()) {
            updateCalendarDisplay();
            updateTaskSidebar();
        }
    }
    
    private void deleteTask(Task task) {
        // Find and remove task from its goal
        for (Goal goal : goalService.getAllGoals()) {
            if (goal.getTasks().contains(task)) {
                goal.removeTask(task);
                break;
            }
        }
        
        goalService.saveGoalsToFile();
        updateCalendarDisplay();
        updateTaskSidebar();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Task Deleted");
        alert.setHeaderText(null);
        alert.setContentText("Task has been deleted successfully.");
        alert.showAndWait();
    }
    
    private String getPriorityIcon(Task.Priority priority) {
        switch (priority) {
            case URGENT: return "🚨";
            case HIGH: return "🔴";
            case MEDIUM: return "🟡";
            case LOW: return "🟢";
            default: return "⚪";
        }
    }
    
    private String getStatusIcon(Task.TaskStatus status) {
        switch (status) {
            case TO_DO: return "📋";
            case IN_PROGRESS: return "🔄";
            case DONE: return "✅";
            default: return "❓";
        }
    }
    
    // Styling methods
    private String getBackgroundStyle() {
        return "-fx-background-color: #ffffff;";
    }
    
    private String getCalendarContainerStyle() {
        return isDarkTheme ?
            "-fx-background-color: #34495e; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);" :
            "-fx-background-color: white; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);";
    }
    
    private String getCalendarGridStyle() {
        return isDarkTheme ?
            "-fx-background-color: #ffffff; -fx-border-color: #dadce0; -fx-border-width: 1;" :
            "-fx-background-color: #ffffff; -fx-border-color: #dadce0; -fx-border-width: 1;";
    }
    
    private String getHeaderStyle() {
        return isDarkTheme ?
            "-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-color: #dadce0; -fx-border-width: 0 0 1 0;" :
            "-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-color: #dadce0; -fx-border-width: 0 0 1 0;";
    }
    
    private String getNavButtonStyle() {
        return isDarkTheme ?
            "-fx-background-color: transparent; -fx-text-fill: #5f6368; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 50; -fx-padding: 10; -fx-cursor: hand;" +
            "-fx-effect: null; -fx-border-color: transparent;" :
            "-fx-background-color: transparent; -fx-text-fill: #5f6368; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 50; -fx-padding: 10; -fx-cursor: hand;" +
            "-fx-effect: null; -fx-border-color: transparent;";
    }
    
    private String getMonthYearLabelStyle() {
        return isDarkTheme ?
            "-fx-font-size: 22px; -fx-text-fill: #3c4043; -fx-font-weight: normal; -fx-font-family: 'Google Sans', 'Roboto', sans-serif;" :
            "-fx-font-size: 22px; -fx-text-fill: #3c4043; -fx-font-weight: normal; -fx-font-family: 'Google Sans', 'Roboto', sans-serif;";
    }
    
    private String getTodayButtonStyle() {
        return isDarkTheme ?
            "-fx-background-color: transparent; -fx-text-fill: #1a73e8; -fx-font-size: 14px; -fx-background-radius: 4; -fx-padding: 8 16; -fx-cursor: hand; -fx-border-color: #dadce0; -fx-border-width: 1; -fx-border-radius: 4;" :
            "-fx-background-color: transparent; -fx-text-fill: #1a73e8; -fx-font-size: 14px; -fx-background-radius: 4; -fx-padding: 8 16; -fx-cursor: hand; -fx-border-color: #dadce0; -fx-border-width: 1; -fx-border-radius: 4;";
    }
    
    private String getSyncButtonStyle() {
        return isDarkTheme ?
            "-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 4; -fx-padding: 8 16; -fx-cursor: hand;" :
            "-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 4; -fx-padding: 8 16; -fx-cursor: hand;";
    }
    
    private String getDayLabelsStyle() {
        return isDarkTheme ?
            "-fx-background-color: #ffffff; -fx-border-color: #dadce0; -fx-border-width: 0 0 1 0;" :
            "-fx-background-color: #ffffff; -fx-border-color: #dadce0; -fx-border-width: 0 0 1 0;";
    }
    
    private String getDayLabelStyle() {
        return isDarkTheme ?
            "-fx-font-size: 11px; -fx-text-fill: #70757a; -fx-font-weight: 500; -fx-padding: 12 0; -fx-font-family: 'Google Sans', 'Roboto', sans-serif;" :
            "-fx-font-size: 11px; -fx-text-fill: #70757a; -fx-font-weight: 500; -fx-padding: 12 0; -fx-font-family: 'Google Sans', 'Roboto', sans-serif;";
    }
    
    private String getDayCellStyle(LocalDate date, boolean isCurrentMonth) {
        String baseStyle = "-fx-background-color: #ffffff; -fx-border-color: #dadce0; -fx-border-width: 0.5; -fx-cursor: hand; -fx-padding: 8;";
        
        // Highlight today with Google Calendar blue
        if (date.equals(LocalDate.now())) {
            baseStyle = "-fx-background-color: #1a73e8; -fx-border-color: #dadce0; -fx-border-width: 0.5; -fx-cursor: hand; -fx-padding: 8;";
        }
        
        // Highlight selected date
        if (date.equals(selectedDate) && !date.equals(LocalDate.now())) {
            baseStyle = "-fx-background-color: #e8f0fe; -fx-border-color: #1a73e8; -fx-border-width: 1; -fx-cursor: hand; -fx-padding: 8;";
        }
        
        return baseStyle;
    }
    
    private String getDayCellHoverStyle(LocalDate date, boolean isCurrentMonth) {
        if (date.equals(LocalDate.now())) {
            return "-fx-background-color: #1557b0; -fx-border-color: #dadce0; -fx-border-width: 0.5; -fx-cursor: hand; -fx-padding: 8;";
        } else {
            return "-fx-background-color: #f1f3f4; -fx-border-color: #dadce0; -fx-border-width: 0.5; -fx-cursor: hand; -fx-padding: 8;";
        }
    }
    
    private String getDayNumberStyle(LocalDate date, boolean isCurrentMonth) {
        String baseStyle = "-fx-font-size: 14px; -fx-font-weight: normal; -fx-font-family: 'Google Sans', 'Roboto', sans-serif;";
        
        if (date.equals(LocalDate.now())) {
            // Today - white text on blue background
            baseStyle += " -fx-text-fill: #ffffff;";
        } else if (!isCurrentMonth) {
            // Previous/next month dates - lighter gray
            baseStyle += " -fx-text-fill: #dadce0;";
        } else {
            // Current month dates - normal black
            baseStyle += " -fx-text-fill: #3c4043;";
        }
        
        return baseStyle;
    }
    
    private String getTaskLabelStyle(Task task) {
        String baseStyle = "-fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 12; -fx-text-fill: white; -fx-max-width: 110; -fx-font-family: 'Google Sans', 'Roboto', sans-serif;";
        
        switch (task.getStatus()) {
            case DONE:
                return baseStyle + " -fx-background-color: #34a853;"; // Google green
            case IN_PROGRESS:
                return baseStyle + " -fx-background-color: #fbbc04;"; // Google yellow
            default:
                return baseStyle + " -fx-background-color: #4285f4;"; // Google blue
        }
    }
    
    private String getMoreTasksLabelStyle() {
        return isDarkTheme ?
            "-fx-font-size: 10px; -fx-text-fill: #bdc3c7; -fx-font-style: italic;" :
            "-fx-font-size: 10px; -fx-text-fill: #6c757d; -fx-font-style: italic;";
    }
    
    private String getTaskSidebarStyle() {
        return isDarkTheme ?
            "-fx-background-color: #2c3e50; -fx-border-color: #34495e; -fx-border-width: 1 0 0 1;" :
            "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 1;";
    }
    
    private String getSidebarTitleStyle() {
        return isDarkTheme ?
            "-fx-font-size: 16px; -fx-text-fill: #ecf0f1; -fx-font-weight: bold;" :
            "-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;";
    }
    
    private String getAddTaskButtonStyle() {
        return isDarkTheme ?
            "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 20; -fx-min-width: 30; -fx-min-height: 30;" :
            "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 20; -fx-min-width: 30; -fx-min-height: 30;";
    }
    
    private String getNoTasksLabelStyle() {
        return isDarkTheme ?
            "-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-style: italic; -fx-padding: 20;" :
            "-fx-font-size: 14px; -fx-text-fill: #6c757d; -fx-font-style: italic; -fx-padding: 20;";
    }
    
    private String getTaskCardStyle(Task task) {
        String baseStyle = isDarkTheme ?
            "-fx-background-color: #34495e; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1); -fx-cursor: hand;" :
            "-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1); -fx-cursor: hand;";
        
        return baseStyle;
    }
    
    private String getTaskTitleStyle(Task task) {
        String baseStyle = isDarkTheme ?
            "-fx-font-size: 13px; -fx-text-fill: #ecf0f1; -fx-font-weight: bold;" :
            "-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;";
        
        if (task.getStatus() == Task.TaskStatus.DONE) {
            baseStyle += " -fx-strikethrough: true;";
        }
        
        return baseStyle;
    }
    
    private String getTaskDescriptionStyle() {
        return isDarkTheme ?
            "-fx-font-size: 11px; -fx-text-fill: #bdc3c7;" :
            "-fx-font-size: 11px; -fx-text-fill: #6c757d;";
    }
}
