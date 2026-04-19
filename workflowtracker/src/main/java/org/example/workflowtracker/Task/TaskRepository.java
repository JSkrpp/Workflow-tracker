package org.example.workflowtracker.Task;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Integer> {
	Optional<Task> findByIdAndProjectId(Integer id, Integer projectId);
}
