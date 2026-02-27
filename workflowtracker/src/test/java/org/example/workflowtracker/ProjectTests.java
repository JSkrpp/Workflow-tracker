package org.example.workflowtracker;
 

import org.example.workflowtracker.project.Project;
import org.example.workflowtracker.Task.Task;
import org.example.workflowtracker.user.User;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class ProjectTests {

    @Test
    void testObjectCreation() {
        Project project = new Project();
        assertNotNull(project);
    }

    @Test
    void shouldHandleEmptyName() {
        Project project = new Project();
        project.setName("");
        assertEquals("", project.getName());
    }

    @Test
    void shouldHandleNullName() {
        Project project = new Project();
        project.setName(null);
        assertNull(project.getName());
    }

    @Test
    void shouldHandleVeryLongName() {
        Project project = new Project();
        String longName = "A".repeat(1000);
        project.setName(longName);
        assertEquals(longName, project.getName());
    }

    @Test
    void shouldHandleNameWithSpecialCharacters() {
        Project project = new Project();
        project.setName("Проект <script>alert('xss')</script> 日本語");
        assertEquals("Проект <script>alert('xss')</script> 日本語", project.getName());
    }

    @Test
    void shouldHandleNameWithWhitespaceOnly() {
        Project project = new Project();
        project.setName("   ");
        assertEquals("   ", project.getName());
    }

    @Test
    void shouldAllowChangingOwner() {
        Project project = new Project();
        User owner1 = new User();
        User owner2 = new User();
        project.setCreatedBy(owner1);
        project.setCreatedBy(owner2);
        assertEquals(owner2, project.getCreatedBy());
    }

    @Test
    void shouldAllowNullOwner() {
        Project project = new Project();
        project.setCreatedBy(new User());
        project.setCreatedBy(null);
        assertNull(project.getCreatedBy());
    }

    @Test
void shouldHandleEmptyTaskList() {
    Project project = new Project();
    assertNotNull(project.getTasks()); // or assertNull depending on init
    assertTrue(project.getTasks().isEmpty());
}

@Test
void shouldAddTaskToProject() {
    Project project = new Project();
    Task task = new Task();
    task.setTitle("Fix bug");
    project.getTasks().add(task);
    assertEquals(1, project.getTasks().size());
}

@Test
void shouldHandleDuplicateTasksInList() {
    Project project = new Project();
    Task task = new Task();
    project.getTasks().add(task);
    project.getTasks().add(task);
    assertEquals(2, project.getTasks().size());
}
}    
