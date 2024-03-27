package dev.mikita.automatewizard.repository;

import dev.mikita.automatewizard.entity.Scenario;
import dev.mikita.automatewizard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface ScenarioRepository extends JpaRepository<Scenario, String> {
    Optional<Scenario> findById(UUID id);
    Optional<List<Scenario>> findAllByOwner(User user);
}
