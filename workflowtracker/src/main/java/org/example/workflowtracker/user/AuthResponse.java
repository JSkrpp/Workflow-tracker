package org.example.workflowtracker.user;

public record AuthResponse(
        String token,
        Integer userId,
        String email,
        String displayName
) {
}
