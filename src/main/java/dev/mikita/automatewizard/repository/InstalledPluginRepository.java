package dev.mikita.automatewizard.repository;

import dev.mikita.automatewizard.entity.InstalledPlugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface InstalledPluginRepository extends JpaRepository<InstalledPlugin, String> {
    Optional<InstalledPlugin> findByPluginIdAndUserId(UUID pluginId, UUID userId);
}