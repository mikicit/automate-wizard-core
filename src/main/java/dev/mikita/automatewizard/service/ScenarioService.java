package dev.mikita.automatewizard.service;

import dev.mikita.automatewizard.dto.request.CreateScenarioRequest;
import dev.mikita.automatewizard.dto.request.TaskRequest;
import dev.mikita.automatewizard.entity.*;
import dev.mikita.automatewizard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {
    private final TriggerRepository triggerRepository;
    private final WebhookRepository webhookRepository;
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

        // Clear run type
        clearRunType(scenario, user);

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

    @Transactional
    public ScenarioRunType updateRunType(UUID id, ScenarioRunType runType, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        }

        if (scenario.getRunType() == runType) {
            return scenario.getRunType();
        }

        // Clear old run type
        clearRunType(scenario, user);

        // Create webhook if needed
        if (runType == ScenarioRunType.WEBHOOK) {
            var webhook = Webhook.builder()
                    .scenario(scenario)
                    .build();
            scenario.setWebhook(webhook);
        }

        scenario.setRunType(runType);
        scenarioRepository.save(scenario);

        return scenario.getRunType();
    }

    @Transactional
    public Trigger updateTrigger(UUID id, UUID triggerId, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));

        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        } else if (scenario.getRunType() != ScenarioRunType.TRIGGER) {
            throw new RuntimeException("Scenario is not trigger-based");
        }

        var oldTrigger = scenario.getTrigger();
        var newTrigger = triggerRepository.findById(triggerId).orElseThrow(() -> new RuntimeException("Trigger not found"));

        // Check if plugin is installed
        if (!installedPluginRepository.existsByPluginIdAndUserId(newTrigger.getPlugin().getId(), user.getId())) {
            throw new RuntimeException("Plugin %s not installed".formatted(newTrigger.getPlugin().getName()));
        }

        if (oldTrigger != null) {
            // Check if trigger is the same
            if (oldTrigger.getId().equals(newTrigger.getId())) {
                return oldTrigger;
            } else {
                triggerUnsubscribe(scenario, user);
            }
        }

        triggerSubscribe(scenario, newTrigger, user);
        return newTrigger;
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

        // Clear old tasks
        scenario.getTasks().clear();

        // Create new tasks
        List<Task> taskEntities = tasks.stream().map(taskRequest -> Task.builder()
                .scenario(scenario)
                .action(actionRepository.findById(taskRequest.getActionId())
                        .orElseThrow(() -> new RuntimeException("Action not found")))
                .preprocessor(taskRequest.getPreprocessor())
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
    public void runScenarioManual(UUID id, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));

        // Validation
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        } else if (scenario.getState() == ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is inactive");
        } else if (scenario.getRunType() != ScenarioRunType.MANUAL) {
            throw new RuntimeException("Scenario is not manual");
        }

        scenarioExecutionService.runScenario(scenario.getId(), new HashMap<>());
    }

    @Transactional(readOnly = true)
    public void taskHandler(UUID taskExecutionId, Map<String, Object> payload) {
        var taskExecution = taskExecutionRepository.findById(taskExecutionId)
                .orElseThrow(() -> new RuntimeException("Task execution not found"));

        // Validation
        if (taskExecution.getState() != TaskExecutionState.STARTED) {
            throw new RuntimeException("Task execution is not started");
        }

        scenarioExecutionService.taskHandler(taskExecution.getId(), payload);
    }

    @Transactional(readOnly = true)
    public void triggerHandler(UUID scenarioId, Map<String, Object> payload) {
        var scenario = scenarioRepository.findById(scenarioId).orElseThrow(() -> new RuntimeException("Scenario not found"));

        if (scenario.getState() == ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is inactive");
        } else if (scenario.getRunType() != ScenarioRunType.TRIGGER) {
            throw new RuntimeException("Scenario is not trigger-based");
        }

        scenarioExecutionService.runScenario(scenario.getId(), payload);
    }

    @Transactional(readOnly = true)
    public void webhookHandler(UUID webhookId, Map<String, Object> payload) {
        var webhook = webhookRepository.findById(webhookId).orElseThrow(() -> new RuntimeException("Webhook not found"));
        var scenario = webhook.getScenario();

        if (scenario.getState() == ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is inactive");
        } else if (scenario.getRunType() != ScenarioRunType.WEBHOOK) {
            throw new RuntimeException("Scenario is not webhook-based");
        }

        scenarioExecutionService.runScenario(scenario.getId(), payload);
    }

    private void triggerSubscribe(Scenario scenario, Trigger trigger, User user) {
        scenario.setTrigger(trigger);

        // Subscribe to trigger
        ClientResponse response = webClientBuilder.baseUrl(trigger.getPlugin().getUrl()).build().post()
                .uri("/triggers/%s".formatted(trigger.getName()))
                .header("X-User-ID", user.getId().toString())
                .header("X-Scenario-ID", scenario.getId().toString())
                .exchange()
                .block();

        if (response == null || response.statusCode().isError()) {
            throw new RuntimeException("Failed to subscribe to trigger");
        }
    }

    private void triggerUnsubscribe(Scenario scenario, User user) {
        var trigger = scenario.getTrigger();

        // Unsubscribe from trigger
        ClientResponse response = webClientBuilder.baseUrl(trigger.getPlugin().getUrl()).build().delete()
                .uri("/triggers/%s".formatted(trigger.getName()))
                .header("X-User-ID", user.getId().toString())
                .header("X-Scenario-ID", scenario.getId().toString())
                .exchange()
                .block();

        if (response == null || response.statusCode().isError()) {
            throw new RuntimeException("Failed to unsubscribe from trigger");
        }

        scenario.setTrigger(null);
    }

    private void clearRunType(Scenario scenario, User user) {
        // Clear trigger if exists
        if (scenario.getTrigger() != null) {
            triggerUnsubscribe(scenario, user);
        }

        // Clear user's webhook if exists
        if (scenario.getWebhook() != null) {
            scenario.setWebhook(null);
        }
    }
}
