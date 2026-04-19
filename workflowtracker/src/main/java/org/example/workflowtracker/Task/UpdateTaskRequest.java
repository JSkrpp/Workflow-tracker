package org.example.workflowtracker.Task;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @Size(max = 20, message = "Task status must be <= 20 characters")
        String status,

        @Positive(message = "Assignee user id must be positive")
        Integer assigneeUserId
) {}
