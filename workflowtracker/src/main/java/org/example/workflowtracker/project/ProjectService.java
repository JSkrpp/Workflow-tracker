package org.example.workflowtracker.project;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.example.workflowtracker.Task.CreateTaskRequest;
import org.example.workflowtracker.Task.Task;
import org.example.workflowtracker.Task.TaskRepository;
import org.example.workflowtracker.Task.TaskStatus;
import org.example.workflowtracker.Task.UpdateTaskRequest;
import org.example.workflowtracker.user.CurrentUserService;
import org.example.workflowtracker.user.User;
import org.example.workflowtracker.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            ProjectMemberRepository projectMemberRepository,
            CurrentUserService currentUserService,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
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

    @Transactional(readOnly = true)
    public Optional<List<ProjectMemberSummary>> findMembers(Integer projectId) {
        Integer id = Objects.requireNonNull(projectId, "Project id cannot be null");
        return projectRepository.findById(id)
                .map(project -> projectMemberRepository.findByProjectIdOrderByUser_DisplayNameAsc(id).stream()
                        .map(member -> new ProjectMemberSummary(
                                member.getUser().getId(),
                                member.getUser().getDisplayName(),
                                member.getUser().getEmail(),
                                member.getRole().name()
                        ))
                        .toList());
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

                    Integer assigneeUserId = request.assigneeUserId();
                    if (assigneeUserId != null) {
                        if (!projectMemberRepository.existsByProjectIdAndUserId(id, assigneeUserId)) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee must be a member of the project");
                        }

                        User assignee = userRepository.findById(assigneeUserId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee user not found"));
                        task.setAssignee(assignee);
                    }

                    return taskRepository.save(task);
                });
    }

    @Transactional
    public Optional<Task> updateTask(Integer projectId, Integer taskId, UpdateTaskRequest request) {
        Integer id = Objects.requireNonNull(projectId, "Project id cannot be null");
        Integer targetTaskId = Objects.requireNonNull(taskId, "Task id cannot be null");
        User currentUser = currentUserService.getCurrentUserOrThrow();

        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a project member"));

        return taskRepository.findById(targetTaskId)
            .filter(task -> task.getProject() != null && Objects.equals(task.getProject().getId(), id))
                .map(task -> {
                    boolean statusProvided = request.status() != null;
                    boolean assigneeProvided = request.assigneeUserId() != null;

                    if (!statusProvided && !assigneeProvided) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide status and/or assigneeUserId");
                    }

                    if (statusProvided) {
                        if (task.getAssignee() == null || !Objects.equals(task.getAssignee().getId(), currentUser.getId())) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assignee can change task status");
                        }

                        String rawStatus = request.status();
                        if (rawStatus.isBlank()) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task status cannot be blank");
                        }
                        task.setStatus(TaskStatus.from(rawStatus));
                    }

                    if (assigneeProvided) {
                        if (membership.getRole() != ProjectMemberRole.OWNER) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can change assignee");
                        }

                        Integer assigneeUserId = request.assigneeUserId();
                        if (!projectMemberRepository.existsByProjectIdAndUserId(id, assigneeUserId)) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee must be a member of the project");
                        }

                        User assignee = userRepository.findById(assigneeUserId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee user not found"));
                        task.setAssignee(assignee);
                    }

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