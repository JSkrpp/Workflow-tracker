package org.example.workflowtracker.project;

import java.util.List;

import org.example.workflowtracker.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    @Query("""
            select distinct p
            from Project p
            where p.createdBy = :user
               or exists (
                    select 1 from ProjectMember pm
                    where pm.project = p
                      and pm.user = :user
               )
            """)
    List<Project> findAccessibleProjects(@Param("user") User user);
}