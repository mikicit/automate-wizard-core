package dev.mikita.automatewizard.service;

import com.fasterxml.jackson.databind.JsonNode;
import dev.mikita.automatewizard.dto.request.CreateScenarioRequest;
import dev.mikita.automatewizard.dto.request.TaskRequest;
import dev.mikita.automatewizard.entity.Trigger;
import dev.mikita.automatewizard.entity.*;
import dev.mikita.automatewizard.jobs.ScenarioRunningJob;
import dev.mikita.automatewizard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.quartz.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final Scheduler quartzScheduler;

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

    @Transactional(readOnly = true)
    public List<TaskExecution> getTaskExecutions(UUID scenarioId, UUID executionId, User user) {
        var scenarioExecution = scenarioExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Scenario execution not found"));

        if (!scenarioExecution.getScenario().getOwner().equals(user)
                || !scenarioExecution.getScenario().getId().equals(scenarioId)) {
            throw new RuntimeException("Scenario execution not found");
        }

        Hibernate.initialize(scenarioExecution.getTasks());

        return scenarioExecution.getTasks();
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

        // Check if state is the same
        if (scenario.getState() == state) {
            return scenario.getState();
        }

        // Validation and activation
        if (state == ScenarioState.ACTIVE) {
            if (scenario.getTasks().isEmpty()) {
                throw new RuntimeException("Tasks not set");
            }

            switch (scenario.getRunType()) {
                case TRIGGER -> {
                    if (scenario.getTrigger() == null) {
                        throw new RuntimeException("Trigger not set");
                    }
                    triggerSubscribe(scenario, scenario.getTrigger(), scenario.getTriggerPayload(), user);
                }
                case SCHEDULE -> {
                    if (scenario.getSchedule() == null) {
                        throw new RuntimeException("Schedule not set");
                    }

                    try {
                        quartzScheduler.resumeJob(JobKey.jobKey("scenario-%s".formatted(scenario.getId())));
                    } catch (SchedulerException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            switch (scenario.getRunType()) {
                case TRIGGER -> {
                    triggerUnsubscribe(scenario, scenario.getTrigger(), user);
                }
                case SCHEDULE -> {
                    try {
                        quartzScheduler.pauseJob(JobKey.jobKey("scenario-%s".formatted(scenario.getId())));
                    } catch (SchedulerException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        scenario.setState(state);
        return scenario.getState();
    }

    @Transactional
    public ScenarioRunType updateRunType(UUID id, ScenarioRunType runType, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));

        // Validation
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        } else if (scenario.getState() != ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is not inactive");
        }

        // Check if run type is the same
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
    public Trigger updateTrigger(UUID id, UUID triggerId, JsonNode payload, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));

        // Validation
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        } else if (scenario.getRunType() != ScenarioRunType.TRIGGER) {
            throw new RuntimeException("Scenario is not trigger-based");
        } else if (scenario.getState() != ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is not inactive");
        }

        var newTrigger = triggerRepository.findById(triggerId).orElseThrow(() -> new RuntimeException("Trigger not found"));

        // Check if trigger is the same
        if (scenario.getTrigger() != null && !scenario.getTrigger().getId().equals(newTrigger.getId())) {
            return scenario.getTrigger();
        }

        // Check if plugin is installed
        if (!installedPluginRepository.existsByPluginIdAndUserId(newTrigger.getPlugin().getId(), user.getId())) {
            throw new RuntimeException("Plugin %s not installed".formatted(newTrigger.getPlugin().getName()));
        }

        scenario.setTrigger(newTrigger);
        scenario.setTriggerPayload(payload);
        return newTrigger;
    }

    @Transactional
    public String updateSchedule(UUID id, String cron, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));

        // Validation
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        } else if (scenario.getRunType() != ScenarioRunType.SCHEDULE) {
            throw new RuntimeException("Scenario is not schedule-based");
        } else if (scenario.getState() != ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is not inactive");
        }

        // Create job
        var jobDetail = JobBuilder.newJob(ScenarioRunningJob.class)
                .withIdentity("scenario-%s".formatted(scenario.getId()))
                .usingJobData("scenarioId", scenario.getId().toString())
                .storeDurably(true)
                .build();

        // Create trigger
        var trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("scenario-%s".formatted(scenario.getId()))
                .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();

        try {
            if (quartzScheduler.checkExists(jobDetail.getKey())) {
                quartzScheduler.deleteJob(jobDetail.getKey());
            }

            quartzScheduler.addJob(jobDetail, true);
            quartzScheduler.scheduleJob(trigger);
            quartzScheduler.pauseJob(jobDetail.getKey());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        scenario.setSchedule(cron);

        return scenario.getSchedule();
    }

    @Transactional(readOnly = true)
    public List<Task> getTasks(UUID id, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        }

        Hibernate.initialize(scenario.getTasks());

        return scenario.getTasks();
    }

    @Transactional
    public List<Task> updateTasks(UUID id, List<TaskRequest> tasks, User user) {
        var scenario = scenarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Scenario not found"));

        // Validation
        if (!scenario.getOwner().equals(user)) {
            throw new RuntimeException("Scenario not found");
        } else if (scenario.getState() != ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is not inactive");
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
    public void processTaskExecution(UUID taskExecutionId, Map<String, Object> payload) {
        var taskExecution = taskExecutionRepository.findById(taskExecutionId)
                .orElseThrow(() -> new RuntimeException("Task execution not found"));

        // Validation
        if (taskExecution.getState() != TaskExecutionState.STARTED) {
            throw new RuntimeException("Task execution is not started");
        }

        scenarioExecutionService.taskHandler(taskExecution.getId(), payload);
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
    public void runScenarioByTrigger(UUID scenarioId, Map<String, Object> payload) {
        var scenario = scenarioRepository.findById(scenarioId).orElseThrow(() -> new RuntimeException("Scenario not found"));

        // Validation
        if (scenario.getState() == ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is inactive");
        } else if (scenario.getRunType() != ScenarioRunType.TRIGGER) {
            throw new RuntimeException("Scenario is not trigger-based");
        }

        scenarioExecutionService.runScenario(scenario.getId(), payload);
    }

    @Transactional(readOnly = true)
    public void runScenarioByWebhook(UUID webhookId, Map<String, Object> payload) {
        var webhook = webhookRepository.findById(webhookId).orElseThrow(() -> new RuntimeException("Webhook not found"));
        var scenario = webhook.getScenario();

        // Validation
        if (scenario.getState() == ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is inactive");
        } else if (scenario.getRunType() != ScenarioRunType.WEBHOOK) {
            throw new RuntimeException("Scenario is not webhook-based");
        }

        scenarioExecutionService.runScenario(scenario.getId(), payload);
    }

    @Transactional(readOnly = true)
    public void runScenarioBySchedule(UUID scenarioId) {
        var scenario = scenarioRepository.findById(scenarioId).orElseThrow(() -> new RuntimeException("Scenario not found"));

        // Validation
        if (scenario.getState() == ScenarioState.INACTIVE) {
            throw new RuntimeException("Scenario is inactive");
        } else if (scenario.getRunType() != ScenarioRunType.SCHEDULE) {
            throw new RuntimeException("Scenario is not schedule-based");
        }

        scenarioExecutionService.runScenario(scenario.getId(), new HashMap<>());
    }

    private void triggerSubscribe(Scenario scenario, Trigger trigger, JsonNode payload, User user) {
        var response = webClientBuilder.baseUrl(trigger.getPlugin().getUrl()).build().post()
                .uri("/triggers/%s".formatted(trigger.getName()))
                .header("X-User-ID", user.getId().toString())
                .header("X-Scenario-ID", scenario.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .block();

        if (response == null || response.statusCode().isError()) {
            throw new RuntimeException("Failed to subscribe to trigger");
        }
    }

    private void triggerUnsubscribe(Scenario scenario, Trigger trigger, User user) {
        var response = webClientBuilder.baseUrl(trigger.getPlugin().getUrl()).build().delete()
                .uri("/triggers/%s".formatted(trigger.getName()))
                .header("X-User-ID", user.getId().toString())
                .header("X-Scenario-ID", scenario.getId().toString())
                .exchange()
                .block();

        if (response == null || response.statusCode().isError()) {
            throw new RuntimeException("Failed to unsubscribe from trigger");
        }
    }

    private void clearRunType(Scenario scenario, User user) {
        switch (scenario.getRunType()) {
            case TRIGGER -> {
                if (scenario.getTrigger() != null) {
                    triggerUnsubscribe(scenario, scenario.getTrigger(), user);
                    scenario.setTriggerPayload(null);
                    scenario.setTrigger(null);
                }
            }
            case WEBHOOK -> scenario.setWebhook(null);
            case SCHEDULE -> {
                try {
                    var jobKey = JobKey.jobKey("scenario-%s".formatted(scenario.getId()));
                    if (quartzScheduler.checkExists(jobKey)) {
                        quartzScheduler.deleteJob(jobKey);
                    }
                    scenario.setSchedule(null);
                } catch (SchedulerException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
