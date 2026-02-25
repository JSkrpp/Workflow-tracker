package org.example.workflowtracker.project;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.example.workflowtracker.user.CurrentUserService;
import org.example.workflowtracker.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Handles project invitation lifecycle operations:
 * creation, listing, acceptance, and decline.
 *
 * Access is restricted to project owners for invitation management,
 * while recipients can manage invitations addressed to their own email.
 */
@Service
public class ProjectInvitationService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectInvitationRepository projectInvitationRepository;
    private final CurrentUserService currentUserService;

    public ProjectInvitationService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectInvitationRepository projectInvitationRepository,
            CurrentUserService currentUserService
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectInvitationRepository = projectInvitationRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Creates a new pending invitation for a project.
     * Only project owners are allowed to invite members.
     */
    @Transactional
    public ProjectInvitationResponse createInvitation(Integer projectId, CreateProjectInvitationRequest request) {
        Integer id = Objects.requireNonNull(projectId, "Project id cannot be null");
        User currentUser = currentUserService.getCurrentUserOrThrow();

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));


        /*Chech if sender is in the project and is the project owner*/
        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owner can invite"));
        if (membership.getRole() != ProjectMemberRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owner can invite");
        }

        String inviteEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (projectInvitationRepository.existsByProjectIdAndEmailIgnoreCaseAndStatus(
            id,
                inviteEmail,
                ProjectInvitationStatus.PENDING
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Active invite already exists for this email");
        }

        ProjectMemberRole role = resolveRole(request.role());

        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProject(project);
        invitation.setEmail(inviteEmail);
        invitation.setRole(role);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setStatus(ProjectInvitationStatus.PENDING);
        invitation.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invitation.setInvitedBy(currentUser);

        ProjectInvitation saved = projectInvitationRepository.save(invitation);
        return toResponse(saved);
    }

    /*
     * Returns all invitations for the given project ordered by creation time.
     * Visibility is limited to the project owner.
     */
    @Transactional(readOnly = true)
    public List<ProjectInvitationResponse> listProjectInvitations(Integer projectId) {
        Integer id = Objects.requireNonNull(projectId, "Project id cannot be null");
        User currentUser = currentUserService.getCurrentUserOrThrow();

        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owner can view invites"));

        if (membership.getRole() != ProjectMemberRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project owner can view invites");
        }

        return projectInvitationRepository.findByProjectIdOrderByCreatedAtDesc(id)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Lists non-expired pending invitations for the current user.
     */
    @Transactional(readOnly = true)
    public List<ProjectInvitationResponse> listMyPendingInvitations() {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        return projectInvitationRepository.findByEmailIgnoreCaseAndStatusOrderByCreatedAtDesc(
                        currentUser.getEmail(),
                        ProjectInvitationStatus.PENDING
                )
                .stream()
                .filter(invite -> invite.getExpiresAt().isAfter(Instant.now()))
                .map(this::toResponse)
                .toList();
    }

    /**
     * Accepts a pending invitation token and adds the current user as a member
     * when not already part of the project.
     */
    @Transactional
    public SimpleMessageResponse acceptInvitation(String token) {
        User currentUser = currentUserService.getCurrentUserOrThrow();

        ProjectInvitation invitation = projectInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation is no longer active");
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(ProjectInvitationStatus.EXPIRED);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation has expired");
        }

        if (!currentUser.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invitation email does not match current user");
        }

        Integer projectId = invitation.getProject().getId();
        boolean alreadyMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId());
        if (!alreadyMember) {
            ProjectMember member = new ProjectMember();
            member.setProject(invitation.getProject());
            member.setUser(currentUser);
            member.setRole(invitation.getRole() == ProjectMemberRole.OWNER ? ProjectMemberRole.MEMBER : invitation.getRole());
            projectMemberRepository.save(member);
        }

        invitation.setStatus(ProjectInvitationStatus.ACCEPTED);
        return new SimpleMessageResponse("Invitation accepted");
    }

    /**
     * Declines a pending invitation token for the current user.
     */
    @Transactional
    public SimpleMessageResponse declineInvitation(String token) {
        User currentUser = currentUserService.getCurrentUserOrThrow();

        ProjectInvitation invitation = projectInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        if (!currentUser.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invitation email does not match current user");
        }

        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation is no longer active");
        }

        invitation.setStatus(ProjectInvitationStatus.DECLINED);
        return new SimpleMessageResponse("Invitation declined");
    }

    /**
     * Resolves requested invitation role.
     * Defaults to MEMBER and rejects OWNER assignment via invites.
     */
    private ProjectMemberRole resolveRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return ProjectMemberRole.MEMBER;
        }

        String normalized = rawRole.trim().toUpperCase(Locale.ROOT);
        ProjectMemberRole role = ProjectMemberRole.valueOf(normalized);
        if (role == ProjectMemberRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invites can only assign MEMBER role");
        }
        return role;
    }

    /**
     * Maps entity model to API response DTO.
     */
    private ProjectInvitationResponse toResponse(ProjectInvitation invitation) {
        return new ProjectInvitationResponse(
                invitation.getId(),
                invitation.getProject().getId(),
                invitation.getProject().getKey(),
                invitation.getProject().getName(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getToken(),
                invitation.getStatus(),
                invitation.getExpiresAt()
        );
    }
}
