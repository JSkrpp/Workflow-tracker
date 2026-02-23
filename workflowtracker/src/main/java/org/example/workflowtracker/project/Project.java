package org.example.workflowtracker.project;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.example.workflowtracker.Task.Task;

public class Project {
    private Integer id;
    private String key;
    private String name;
    private String description;
    private Instant createdAt;
    private List<Task> tasks = new ArrayList<>();

    public Project() {}

    public Project(Integer id, String key, String name, String description, Instant createdAt, List<Task> tasks) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.tasks = (tasks == null) ? new ArrayList<>() : tasks;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) {
        this.tasks = (tasks == null) ? new ArrayList<>() : tasks;
    }

    public int addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        int nextIndex = this.tasks.size() + 1;
        task.setId(nextIndex);
        this.tasks.add(task);
        return this.tasks.size();
    }

    public Task getTaskByProjectIndex(int taskIndex) {
        if (taskIndex < 1 || taskIndex > this.tasks.size()) {
            throw new IndexOutOfBoundsException("Invalid project task index: " + taskIndex);
        }
        return this.tasks.get(taskIndex - 1);
    }
}