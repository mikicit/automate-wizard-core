package dev.mikita.automatewizard.repository;

import dev.mikita.automatewizard.entity.Trigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface TriggerRepository extends JpaRepository<Trigger, String> {
    Optional<Trigger> findById(UUID id);
    Optional<List<Trigger>> findAllByPluginId(UUID pluginId);
}
