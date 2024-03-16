package dev.mikita.automatewizard.repository;

import dev.mikita.automatewizard.entity.Plugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface PluginRepository extends JpaRepository<Plugin, String> {
}
