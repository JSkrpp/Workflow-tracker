package org.example.workflowtracker.project;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateProjectInvitationRequest(
        @NotBlank(message = "Invite email is required")
        @Email(message = "Invite email must be valid")
        String email,

        String role
) {
}
