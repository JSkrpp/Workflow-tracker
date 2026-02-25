package org.example.workflowtracker.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 255, message = "Task title must be <= 255 characters")
        String title,

        @Size(max = 4000, message = "Task description must be <= 4000 characters")
        String description,

        @Size(max = 20, message = "Task status must be <= 20 characters")
        String status
) {}
