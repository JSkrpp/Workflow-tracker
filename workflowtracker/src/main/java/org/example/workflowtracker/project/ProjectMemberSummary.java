package org.example.workflowtracker.project;

public record ProjectMemberSummary(
        Integer userId,
        String displayName,
        String email,
        String role
) {}
