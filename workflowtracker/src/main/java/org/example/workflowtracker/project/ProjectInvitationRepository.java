package org.example.workflowtracker.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Integer> {
    Optional<ProjectInvitation> findByToken(String token);

    List<ProjectInvitation> findByProjectIdOrderByCreatedAtDesc(Integer projectId);

    List<ProjectInvitation> findByEmailIgnoreCaseAndStatusOrderByCreatedAtDesc(
            String email,
            ProjectInvitationStatus status
    );

    boolean existsByProjectIdAndEmailIgnoreCaseAndStatus(
            Integer projectId,
            String email,
            ProjectInvitationStatus status
    );
}
