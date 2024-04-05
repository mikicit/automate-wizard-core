package dev.mikita.automatewizard.repository;

import dev.mikita.automatewizard.entity.ScenarioExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface ScenarioExecutionRepository extends JpaRepository<ScenarioExecution, String> {
    Optional<ScenarioExecution> findById(UUID id);
    Optional<List<ScenarioExecution>> findAllByScenarioIdAndScenarioOwnerId(UUID scenarioId, UUID userId);
}