package org.example.workflowtracker.project;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProjectService {
    private final ConcurrentHashMap <Integer, Project> store = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1);

    public Project create(CreateProjectRequest request) {
        Integer id = sequence.getAndIncrement();
        Project project = new Project(
                id,
                request.getKey(),
                request.getName(),
                request.getDescription(),
                Instant.now()
        );
        store.put(id, project);
        return project;
    }

    public List<Project> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Project> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }
}
