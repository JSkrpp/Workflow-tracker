package org.example.workflowtracker;

import org.example.workflowtracker.Task.Task;
import org.example.workflowtracker.Task.TaskStatus;
import java.time.Instant;

import org.example.workflowtracker.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private Task task;
    private Instant now;
    private Project project;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        project = new Project();
        task = new Task(1, "Test Task", "Test Description", TaskStatus.TODO, now, project);
    }

    @Test
    void defaultConstructorCreatesInstance() {
        Task t = new Task();
        assertNotNull(t);
    }

    @Test
    void defaultConstructorSetsStatusTodo() {
        Task t = new Task();
        assertEquals(TaskStatus.TODO, t.getStatus());
    }


    @Test
    void paramConstructorSetsAllFields() {
        assertEquals(1, task.getId());
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals(now, task.getCreatedAt());
        assertSame(project, task.getProject());
    }

    @Test
    void paramConstructorWithAllNulls() {
        Task t = new Task(null, null, null, null, null, null);
        assertNull(t.getId());
        assertNull(t.getTitle());
        assertNull(t.getDescription());
        assertNull(t.getStatus());
        assertNull(t.getCreatedAt());
        assertNull(t.getProject());
    }

    @Test
    void setIdNormal() {
        task.setId(42);
        assertEquals(42, task.getId());
    }

    @Test
    void setIdNull() {
        task.setId(null);
        assertNull(task.getId());
    }

    @Test
    void setIdZero() {
        task.setId(0);
        assertEquals(0, task.getId());
    }

    @Test
    void setIdNegative() {
        task.setId(-5);
        assertEquals(-5, task.getId());
    }

    @Test
    void setIdMaxValue() {
        task.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, task.getId());
    }


    @Test
    void setTitleNormal() {
        task.setTitle("New Title");
        assertEquals("New Title", task.getTitle());
    }

    @Test
    void setTitleNull() {
        task.setTitle(null);
        assertNull(task.getTitle());
    }

    @Test
    void setTitleEmpty() {
        task.setTitle("");
        assertEquals("", task.getTitle());
    }

    @Test
    void setTitleWhitespace() {
        task.setTitle("   ");
        assertEquals("   ", task.getTitle());
    }

    @Test
    void setTitleVeryLong() {
        String longTitle = "A".repeat(1000);
        task.setTitle(longTitle);
        assertEquals(longTitle, task.getTitle());
    }

    @Test
    void setTitleSpecialCharacters() {
        task.setTitle("Títle @#$%^&*() 日本語");
        assertEquals("Títle @#$%^&*() 日本語", task.getTitle());
    }

    @Test
    void setDescriptionNormal() {
        task.setDescription("Updated");
        assertEquals("Updated", task.getDescription());
    }

    @Test
    void setDescriptionNull() {
        task.setDescription(null);
        assertNull(task.getDescription());
    }

    @Test
    void setDescriptionEmpty() {
        task.setDescription("");
        assertEquals("", task.getDescription());
    }

    @Test
    void setDescriptionWhitespace() {
        task.setDescription("  \t\n  ");
        assertEquals("  \t\n  ", task.getDescription());
    }

    @Test
    void setDescriptionVeryLong() {
        String longDesc = "B".repeat(5000);
        task.setDescription(longDesc);
        assertEquals(longDesc, task.getDescription());
    }

    @Test
    void setStatusTodo() {
        task.setStatus(TaskStatus.TODO);
        assertEquals(TaskStatus.TODO, task.getStatus());
    }

    @Test
    void setStatusInProgress() {
        task.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    void setStatusDone() {
        task.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    void setProjectNormal() {
        Project p = new Project();
        task.setProject(p);
        assertSame(p, task.getProject());
    }


    @Test
    void replaceProject() {
        Project p1 = new Project();
        Project p2 = new Project();
        task.setProject(p1);
        assertSame(p1, task.getProject());
        task.setProject(p2);
        assertSame(p2, task.getProject());
    }


}