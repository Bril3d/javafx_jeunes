package com.coach.model;

public enum TaskCategory {
    PROFESSIONAL("Professional"),
    PERSONAL("Personal"),
    EDUCATION("Education"),
    HEALTH("Health"),
    FINANCE("Finance"),
    ADMIN("Administrative"),
    ROUTINE("Routine"),
    OTHER("Other");

    private final String displayName;

    TaskCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static TaskCategory fromString(String text) {
        for (TaskCategory b : TaskCategory.values()) {
            if (b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return OTHER;
    }
}
