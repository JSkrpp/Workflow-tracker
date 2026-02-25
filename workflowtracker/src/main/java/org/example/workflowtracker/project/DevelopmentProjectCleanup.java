package org.example.workflowtracker.project;

import org.example.workflowtracker.Task.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Development utility to clean up all project data at starup
 * Can be enabled/disabled via "app.dev.clean-project-data" property in application.properties
 */

@Component
@ConditionalOnProperty(name = "app.dev.clean-project-data", havingValue = "true")
public class DevelopmentProjectCleanup implements CommandLineRunner {

    private final TaskRepository taskRepository;
    private final ProjectInvitationRepository projectInvitationRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;

    public DevelopmentProjectCleanup(
            TaskRepository taskRepository,
            ProjectInvitationRepository projectInvitationRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectRepository projectRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectInvitationRepository = projectInvitationRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        taskRepository.deleteAllInBatch();
        projectInvitationRepository.deleteAllInBatch();
        projectMemberRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
    }
}
