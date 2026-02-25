package org.example.workflowtracker.project;

import java.time.Instant;

public record ProjectInvitationResponse(
        Integer id,
        Integer projectId,
        String projectKey,
        String projectName,
        String email,
        ProjectMemberRole role,
        String token,
        ProjectInvitationStatus status,
        Instant expiresAt
) {
}
