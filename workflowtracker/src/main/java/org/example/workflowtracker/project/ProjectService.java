package org.example.workflowtracker.project;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.example.workflowtracker.Task.Task;
import org.example.workflowtracker.Task.TaskRepository;
import org.example.workflowtracker.Task.TaskStatus;
import org.example.workflowtracker.user.CurrentUserService;
import org.example.workflowtracker.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;

    public ProjectService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            ProjectMemberRepository projectMemberRepository,
            CurrentUserService currentUserService
    ) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Project create(CreateProjectRequest request) {
        User currentUser = currentUserService.getCurrentUserOrThrow();

        Project project = new Project();
        project.setKey(request.getKey());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedBy(currentUser);

        Project savedProject = projectRepository.save(project);

        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(savedProject);
        ownerMember.setUser(currentUser);
        ownerMember.setRole(ProjectMemberRole.OWNER);
        projectMemberRepository.save(ownerMember);

        return savedProject;
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        return projectRepository.findAccessibleProjects(currentUser);
    }

    @Transactional(readOnly = true)
    public Optional<Project> findById(Integer id) {
        Integer projectId = Objects.requireNonNull(id, "Project id cannot be null");
        return projectRepository.findById(projectId);
    }

    @Transactional(readOnly = true)
    public Optional<List<Task>> findTasks(Integer projectId) {
        Integer id = Objects.requireNonNull(projectId, "Project id cannot be null");
        return projectRepository.findById(id)
                .map(project -> {
                    project.getTasks().size();
                    return project.getTasks();
                });
    }

    @Transactional
    public Optional<Task> addTask(Integer projectId, CreateTaskRequest request) {
        Integer id = Objects.requireNonNull(projectId, "Project id cannot be null");
        return projectRepository.findById(id)
                .map(project -> {
                    Task task = new Task();
                    task.setTitle(request.title().trim());
                    task.setDescription(request.description());
                    task.setStatus(TaskStatus.from(request.status()));
                    task.setProject(project);
                    return taskRepository.save(task);
                });
    }

    @Transactional
    public boolean delete(Integer id) {
        Integer projectId = Objects.requireNonNull(id, "Project id cannot be null");
        if (!projectRepository.existsById(projectId)) {
            return false;
        }
        projectRepository.deleteById(projectId);
        return true;
    }
}