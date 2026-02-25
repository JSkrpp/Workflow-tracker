package org.example.workflowtracker.project;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
    Optional<ProjectMember> findByProjectIdAndUserId(Integer projectId, Integer userId);

    boolean existsByProjectIdAndUserId(Integer projectId, Integer userId);
}
