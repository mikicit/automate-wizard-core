package dev.mikita.automatewizard.service;

import dev.mikita.automatewizard.dto.request.CreateScenarioRequest;
import dev.mikita.automatewizard.entity.Scenario;
import dev.mikita.automatewizard.entity.ScenarioState;
import dev.mikita.automatewizard.entity.User;
import dev.mikita.automatewizard.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;

    public List<Scenario> getScenarios(User user) {
        return scenarioRepository.findAllByOwner(user).orElseThrow(() -> new RuntimeException("Scenarios not found"));
    }

    public Scenario getScenario(UUID id, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        }

        return scenario;
    }

    public Scenario createScenario(CreateScenarioRequest request, User user) {
        var scenario = Scenario.builder()
                .name(request.getName())
                .owner(user)
                .state(ScenarioState.INACTIVE)
                .build();

        return scenarioRepository.save(scenario);
    }

    public void deleteScenario(UUID id, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        }

        scenarioRepository.delete(scenario);
    }
}
