package org.example.workflowtracker.project;

import java.util.List;

import org.example.workflowtracker.Task.Task;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("v1/api/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Project create(@RequestBody CreateProjectRequest request) {
        if (request.getKey() == null || request.getKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project key is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project name is required");
        }
        return service.create(request);
    }

    @GetMapping
    public List<Project> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Project getById(@PathVariable Integer id) {
        return service.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    @GetMapping("/{id}/tasks")
    public List<Task> getTasks(@PathVariable Integer id) {
        return service.findTasks(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    @PostMapping("/{id}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public Task addTask(@PathVariable Integer id, @RequestBody Task task) {
        return service.addTask(id, task)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        boolean deleted = service.delete(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
    }
}