package dev.mikita.automatewizard.repository;

import dev.mikita.automatewizard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String username);
}
