package org.example.workflowtracker.Task;

import io.swagger.v3.oas.annotations.media.Schema;

public class Task {

    @Schema(description = "Unique identifier of the task", example = "1", accessMode=Schema.AccessMode.READ_ONLY)
    private Integer id;
    private String title;
    private String description;
    private String status;


    public Task() {}

    public Task(String title, String description, String status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
