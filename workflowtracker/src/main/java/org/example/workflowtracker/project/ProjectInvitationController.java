package org.example.workflowtracker.project;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("v1/api/invites")
@SecurityRequirement(name = "bearerAuth")
public class ProjectInvitationController {

    private final ProjectInvitationService invitationService;

    public ProjectInvitationController(ProjectInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping("/me")
    public List<ProjectInvitationResponse> myInvites() {
        return invitationService.listMyPendingInvitations();
    }

    @PostMapping("/{token}/accept")
    public SimpleMessageResponse accept(@PathVariable String token) {
        return invitationService.acceptInvitation(token);
    }

    @PostMapping("/{token}/decline")
    public SimpleMessageResponse decline(@PathVariable String token) {
        return invitationService.declineInvitation(token);
    }
}
