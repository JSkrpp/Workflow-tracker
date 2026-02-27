package org.example.workflowtracker.project;
/**
 * DTO for messages only containing String messages. Used for e.g. Accepting/declining project invitations.
 */
public record SimpleMessageResponse(String message) {
}
