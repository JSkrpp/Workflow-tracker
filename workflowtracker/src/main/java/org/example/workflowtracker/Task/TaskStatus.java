package org.example.workflowtracker.Task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus {
    TODO("TODO"),
    IN_PROGRESS("IN PROGRESS"),
    DONE("DONE");

    private final String apiValue;

    TaskStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    @JsonValue
    public String getApiValue() {
        return apiValue;
    }

    @JsonCreator
    public static TaskStatus from(String raw) {
        if (raw == null || raw.isBlank()) return TODO;

        String normalized = raw.trim().toUpperCase().replace(' ', '_');
        return switch (normalized) {
            case "TODO" -> TODO;
            case "IN_PROGRESS" -> IN_PROGRESS;
            case "DONE" -> DONE;
            default -> throw new IllegalArgumentException("Invalid status: " + raw);
        };
    }
}