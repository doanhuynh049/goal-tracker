import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import model.ViewType;

/**
 * Handles the creation and management of the left sidebar menu in the MainView.
 * This class encapsulates all sidebar-related functionality including navigation buttons,
 * styling, and theme management.
 */
public class SidebarMenuView {
    
    private final MainView mainView;
    private final boolean isDarkTheme;
    private VBox sidebar;
    
    public SidebarMenuView(MainView mainView, boolean isDarkTheme) {
        this.mainView = mainView;
        this.isDarkTheme = isDarkTheme;
    }
    
    /**
     * Creates and returns the complete sidebar navigation panel
     */
    public VBox createSidebar() {
        sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle(getSidebarStyle());
        sidebar.setPrefWidth(180);
        sidebar.setMinWidth(180);

        // App Logo/Branding section
        Label sidebarBrand = createSidebarBrand();
        
        // Navigation buttons
        Button dashboardBtn = createSidebarButton("🏠", "Dashboard", ViewType.DASHBOARD);
        Button goalsBtn = createSidebarButton("🎯", "Goals", ViewType.GOALS);
        Button tasksBtn = createSidebarButton("📝", "Tasks", ViewType.TASKS);
        Button taskMgmtBtn = createSidebarButton("🔧", "Add/Edit My Goals", ViewType.TASK_MANAGEMENT);
        Button statsBtn = createSidebarButton("📊", "Statistics", ViewType.STATISTICS);
        Button settingsBtn = createSidebarButton("⚙️", "Settings", ViewType.SETTINGS);

        // Set initial active state for dashboard
        updateSidebarButtonState(dashboardBtn, true);

        // Add all elements to sidebar
        sidebar.getChildren().addAll(
            sidebarBrand,
            new Separator(),
            dashboardBtn,
            goalsBtn,
            tasksBtn,
            taskMgmtBtn,
            statsBtn,
            new Separator(),
            settingsBtn
        );

        return sidebar;
    }
    
    /**
     * Creates the sidebar branding label
     */
    private Label createSidebarBrand() {
        Label sidebarBrand = new Label("Goal Tracker");
        sidebarBrand.setStyle(getSidebarBrandStyle());
        sidebarBrand.setPadding(new Insets(0, 0, 20, 0));
        return sidebarBrand;
    }
    
    /**
     * Creates a sidebar navigation button with the specified icon, text, and view type
     */
    private Button createSidebarButton(String icon, String text, ViewType viewType) {
        Button button = new Button(icon + "  " + text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(getSidebarButtonStyle(false));

        // Set actions for each button
        button.setOnAction(e -> {
            // Update current view and button states
            mainView.setCurrentView(viewType);
            updateAllSidebarButtons();
            updateSidebarButtonState(button, true);
            
            // Handle navigation based on view
            navigateToView(viewType);
        });

        // Hover effects
        button.setOnMouseEntered(e -> {
            if (mainView.getCurrentView() != viewType) {
                button.setStyle(getSidebarButtonHoverStyle());
            }
        });
        button.setOnMouseExited(e -> {
            if (mainView.getCurrentView() != viewType) {
                button.setStyle(getSidebarButtonStyle(false));
            }
        });

        return button;
    }
    
    /**
     * Handles navigation to the specified view type
     */
    private void navigateToView(ViewType viewType) {
        switch (viewType) {
            case DASHBOARD:
                mainView.showDashboard();
                break;
            case GOALS:
                mainView.showGoalsView();
                break;
            case TASKS:
                mainView.showTasksView();
                break;
            case TASK_MANAGEMENT:
                mainView.showTaskManagementView();
                break;
            case STATISTICS:
                mainView.showStatisticsView();
                break;
            case SETTINGS:
                mainView.showSettingsDialog();
                break;
            case REPORTS:
                mainView.showReportsView();
                break;
            default:
                mainView.showDashboard();
                break;
        }
    }
    
    /**
     * Updates all sidebar buttons to inactive state
     */
    public void updateAllSidebarButtons() {
        if (sidebar != null) {
            sidebar.getChildren().forEach(node -> {
                if (node instanceof Button btn) {
                    updateSidebarButtonState(btn, false);
                }
            });
        }
    }
    
    /**
     * Updates a specific sidebar button's active/inactive state
     */
    public void updateSidebarButtonState(Button button, boolean isActive) {
        if (button != null) {
            button.setStyle(getSidebarButtonStyle(isActive));
        }
    }
    
    /**
     * Updates the sidebar theme and all its children components
     */
    public void updateSidebarTheme(ViewType currentView) {
        if (sidebar == null) return;
        
        // Update sidebar style
        sidebar.setStyle(getSidebarStyle());
        
        // Update sidebar buttons and brand
        sidebar.getChildren().forEach(node -> {
            if (node instanceof Button btn) {
                String buttonText = btn.getText();
                boolean isActive = false;
                
                // Check if this button represents the current view
                if ((buttonText.contains("Dashboard") && currentView == ViewType.DASHBOARD) ||
                    (buttonText.contains("Goals") && currentView == ViewType.GOALS) ||
                    (buttonText.contains("Tasks") && !buttonText.contains("Management") && currentView == ViewType.TASKS) ||
                    (buttonText.contains("Add/Edit My Goals") && currentView == ViewType.TASK_MANAGEMENT) ||
                    (buttonText.contains("Statistics") && currentView == ViewType.STATISTICS) ||
                    (buttonText.contains("Settings") && currentView == ViewType.SETTINGS)) {
                    isActive = true;
                }
                
                btn.setStyle(getSidebarButtonStyle(isActive));
            }
            if (node instanceof Label label && label.getText().equals("Goal Tracker")) {
                label.setStyle(getSidebarBrandStyle());
            }
        });
    }
    
    // Styling methods
    private String getSidebarStyle() {
        return isDarkTheme ?
            "-fx-background-color: #2c3e50; -fx-border-color: #34495e; -fx-border-width: 0 1 0 0;" :
            "-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 0 1 0 0;";
    }

    private String getSidebarBrandStyle() {
        return isDarkTheme ?
            "-fx-font-size: 16px; -fx-text-fill: #ecf0f1; -fx-font-weight: bold;" :
            "-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;";
    }

    private String getSidebarButtonStyle(boolean isActive) {
        if (isActive) {
            return isDarkTheme ?
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;" :
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;";
        } else {
            return isDarkTheme ?
                "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;" :
                "-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;";
        }
    }

    private String getSidebarButtonHoverStyle() {
        return isDarkTheme ?
            "-fx-background-color: #34495e; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;" :
            "-fx-background-color: #d5dbdb; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-padding: 12px; -fx-background-radius: 6px; -fx-cursor: hand;";
    }
}
