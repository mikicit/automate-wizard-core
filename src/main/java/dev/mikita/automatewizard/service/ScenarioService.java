package dev.mikita.automatewizard.service;

import dev.mikita.automatewizard.dto.request.CreateScenarioRequest;
import dev.mikita.automatewizard.dto.request.TaskRequest;
import dev.mikita.automatewizard.entity.*;
import dev.mikita.automatewizard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;
    private final ActionRepository actionRepository;
    private final InstalledPluginRepository installedPluginRepository;
    private final TaskExecutionRepository taskExecutionRepository;
    private final ScenarioExecutionRepository scenarioExecutionRepository;
    private final ScenarioExecutionService scenarioExecutionService;
    private final WebClient.Builder webClientBuilder;

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

    public List<ScenarioExecution> getExecutions(UUID scenarioId, User user) {
        var scenario = scenarioRepository.findById(scenarioId).orElseThrow(() -> new RuntimeException("Scenario not found"));
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        }

        return scenarioExecutionRepository.findAllByScenarioIdAndScenarioOwnerId(scenarioId, user.getId())
                .orElseThrow(() -> new RuntimeException("Scenario executions not found"));
    }

    public ScenarioExecution getExecution(UUID scenarioId, UUID executionId, User user) {
        var scenarioExecution = scenarioExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Scenario execution not found"));

        if (!scenarioExecution.getScenario().getOwner().equals(user)
                || !scenarioExecution.getScenario().getId().equals(scenarioId)) {
            throw new RuntimeException("Scenario execution not found");
        }

        return scenarioExecution;
    }

    public List<TaskExecution> getTaskExecutions(UUID scenarioId, UUID executionId, User user) {
        var scenarioExecution = scenarioExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Scenario execution not found"));

        if (!scenarioExecution.getScenario().getOwner().equals(user)
                || !scenarioExecution.getScenario().getId().equals(scenarioId)) {
            throw new RuntimeException("Scenario execution not found");
        }

        return taskExecutionRepository.findAllByScenarioExecutionId(executionId)
                .orElseThrow(() -> new RuntimeException("Task executions not found"));
    }

    public TaskExecution getTaskExecution(UUID scenarioId, UUID executionId, UUID taskExecutionId, User user) {
        var taskExecution = taskExecutionRepository.findById(taskExecutionId)
                .orElseThrow(() -> new RuntimeException("Task execution not found"));

        if (!taskExecution.getScenarioExecution().getScenario().getOwner().equals(user)
                || !taskExecution.getScenarioExecution().getScenario().getId().equals(scenarioId)
                || !taskExecution.getScenarioExecution().getId().equals(executionId)) {
            throw new RuntimeException("Task execution not found");
        }

        return taskExecution;
    }

    public Scenario createScenario(CreateScenarioRequest request, User user) {
        var scenario = Scenario.builder()
                .name(request.getName())
                .owner(user)
                .state(ScenarioState.INACTIVE)
                .runType(ScenarioRunType.MANUAL)
                .build();

        return scenarioRepository.save(scenario);
    }

    @Transactional
    public void deleteScenario(UUID id, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        }

        // TODO: Delete all triggers and executions

        scenarioRepository.delete(scenario);
    }

    @Transactional
    public ScenarioState updateState(UUID id, ScenarioState state, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        }

        scenario.setState(state);
        scenarioRepository.save(scenario);

        return scenario.getState();
    }

    public List<Task> getTasks(UUID id, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        }

        return scenario.getTasks();
    }

    @Transactional
    public List<Task> updateTasks(UUID id, List<TaskRequest> tasks, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        }

        // TODO: Check if there are any running instances of the scenario

        // Clear old tasks
        scenario.getTasks().clear();

        // Create new tasks
        List<Task> taskEntities = tasks.stream().map(taskRequest -> Task.builder()
                .scenario(scenario)
                .action(actionRepository.findById(taskRequest.getActionId())
                        .orElseThrow(() -> new RuntimeException("Action not found")))
                .build()).toList();

        // Check if all plugins are installed
        taskEntities.forEach(task -> {
            if (!installedPluginRepository.existsByPluginIdAndUserId(
                    task.getAction().getPlugin().getId(), user.getId())) {
                throw new RuntimeException("Plugin %s not installed".formatted(task.getAction().getPlugin().getName()));
            }
        });

        scenario.getTasks().addAll(taskEntities);
        scenarioRepository.save(scenario);

        return scenario.getTasks();
    }

    @Transactional(readOnly = true)
    public void runScenario(UUID id, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));

        // Validation
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        } else if (scenario.getState() == ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is inactive");
        } else if (scenario.getRunType() != ScenarioRunType.MANUAL) {
            throw new RuntimeException("Scenario is not manual");
        }

        scenarioExecutionService.runScenario(scenario.getId(), Map.of());
    }

    @Transactional(readOnly = true)
    public void taskHandler(UUID taskExecutionId, UUID scenarioExecutionId, Map<String, Object> payload) {
        var taskExecution = taskExecutionRepository.findById(taskExecutionId)
                .orElseThrow(() -> new RuntimeException("Task execution not found"));

        var scenarioExecution = scenarioExecutionRepository.findById(scenarioExecutionId)
                .orElseThrow(() -> new RuntimeException("Scenario execution not found"));

        // Validation
        if (scenarioExecution.getState() != ScenarioExecutionState.STARTED) {
            throw new RuntimeException("Scenario execution is not started");
        } else if (taskExecution.getState() != TaskExecutionState.STARTED) {
            throw new RuntimeException("Task execution is not started");
        } else if (!taskExecution.getScenarioExecution().getId().equals(scenarioExecutionId)) {
            throw new RuntimeException("Task execution not found");
        }

        scenarioExecutionService.taskHandler(taskExecution.getId(), scenarioExecution.getId(), payload);
    }

    @Transactional(readOnly = true)
    public void triggerHandler(UUID triggerId, UUID scenarioId, Map<String, Object> payload) {
        var scenario = scenarioRepository.findById(scenarioId).orElseThrow(() -> new RuntimeException("Scenario not found"));

        if (scenario.getState() == ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is inactive");
        } else if (scenario.getRunType() != ScenarioRunType.TRIGGER) {
            throw new RuntimeException("Scenario is not trigger-based");
        } else if (!scenario.getTrigger().getId().equals(triggerId)) {
            throw new RuntimeException("Trigger not found");
        }

        scenarioExecutionService.runScenario(scenario.getId(), payload);
    }
}
