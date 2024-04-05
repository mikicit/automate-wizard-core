package dev.mikita.automatewizard.repository;

import dev.mikita.automatewizard.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface TaskRepository extends JpaRepository<Task, String> {
    Optional<Task> findById(UUID id);
}
