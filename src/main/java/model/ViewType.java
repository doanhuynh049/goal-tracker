package model;

/**
 * Enum representing different view types in the Goal & Task Manager application.
 * This provides type safety and extensibility for navigation between different sections.
 */
public enum ViewType {
    DASHBOARD("dashboard", "🏠", "Dashboard"),
    GOALS("goals", "🎯", "Goals"),
    TASKS("tasks", "📝", "Tasks"),
    STATISTICS("statistics", "📊", "Statistics"),
    SETTINGS("settings", "⚙️", "Settings"),
    REPORTS("reports", "📑", "Reports");

    private final String id;
    private final String icon;
    private final String displayName;

    ViewType(String id, String icon, String displayName) {
        this.id = id;
        this.icon = icon;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get ViewType from string ID for backward compatibility
     */
    public static ViewType fromId(String id) {
        for (ViewType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return DASHBOARD; // Default fallback
    }

    /**
     * Check if this view type matches the given string ID
     */
    public boolean matches(String id) {
        return this.id.equals(id);
    }
}
