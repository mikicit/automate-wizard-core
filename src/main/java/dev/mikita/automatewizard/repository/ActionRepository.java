package dev.mikita.automatewizard.repository;

import dev.mikita.automatewizard.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface ActionRepository extends JpaRepository<Action, String> {
    Optional<Action> findById(UUID id);
    Optional<List<Action>> findAllByPluginId(UUID pluginId);
}
