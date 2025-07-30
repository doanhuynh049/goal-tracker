package model;

public enum GoalType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    SHORT_TERM,
    LONG_TERM,
    // Hierarchical types
    LONG_TERM_GOAL,  // 2-3 years
    YEAR_GOAL,       // Yearly goals within long-term
    MONTH_GOAL       // Monthly goals within yearly
}