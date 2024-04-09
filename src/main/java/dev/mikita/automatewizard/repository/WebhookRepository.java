package dev.mikita.automatewizard.repository;

import dev.mikita.automatewizard.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface WebhookRepository extends JpaRepository<Webhook, String> {
    Optional<Webhook> findById(UUID id);
}
