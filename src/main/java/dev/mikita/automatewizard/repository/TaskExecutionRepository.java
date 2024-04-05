package dev.mikita.automatewizard.repository;

import dev.mikita.automatewizard.entity.TaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, String> {
    Optional<TaskExecution> findById(UUID id);
    Optional<List<TaskExecution>> findAllByScenarioExecutionId(UUID id);
}
