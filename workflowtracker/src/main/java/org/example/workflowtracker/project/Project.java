package org.example.workflowtracker.project;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.example.workflowtracker.Task.Task;
import org.example.workflowtracker.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "project_key", nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Task> tasks = new ArrayList<>();

    public Project() {}

    public Project(Integer id, String key, String name, String description, Instant createdAt, List<Task> tasks) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        if (tasks != null) {
            for (Task task : tasks) {
                if (task != null) {
                    this.tasks.add(task);
                }
            }
        }
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<Task> getTasks() { return tasks; }

    public void setTasks(List<Task> tasks) {
        this.tasks.clear();
        if (tasks != null) {
            for (Task task : tasks) {
                if (task != null) {
                    task.setProject(this);
                    this.tasks.add(task);
                }
            }
        }
    }

    public int addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        task.setProject(this);
        this.tasks.add(task);
        return this.tasks.size();
    }

    public void removeTask(Task task) {
        if (task != null && this.tasks.remove(task)) {
            task.setProject(null);
        }
    }

    public Task getTaskByProjectIndex(int taskIndex) {
        if (taskIndex < 1 || taskIndex > this.tasks.size()) {
            throw new IndexOutOfBoundsException("Invalid project task index: " + taskIndex);
        }
        return this.tasks.get(taskIndex - 1);
    }
}