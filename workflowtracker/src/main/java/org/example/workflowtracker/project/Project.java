package org.example.workflowtracker.project;

import java.time.Instant;

public class Project {
    private Integer id;
    private String key;
    private String name;
    private String description;
    private Instant createdAt;

    public Project() {}

    public Project(Integer id, String key, String name, String description, Instant createdAt) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }
    public String getKey() {
        return key;
    }

    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
