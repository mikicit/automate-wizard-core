package dev.mikita.automatewizard.repository;

import dev.mikita.automatewizard.entity.Plugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface PluginRepository extends JpaRepository<Plugin, String> {
    Optional<Plugin> findById (UUID id);
    Optional<List<Plugin>> findAllByAuthorId(UUID authorId);
}
