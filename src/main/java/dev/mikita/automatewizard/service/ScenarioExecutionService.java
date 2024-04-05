package dev.mikita.automatewizard.service;

import dev.mikita.automatewizard.entity.*;
import dev.mikita.automatewizard.repository.ScenarioExecutionRepository;
import dev.mikita.automatewizard.repository.ScenarioRepository;
import dev.mikita.automatewizard.repository.TaskExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioExecutionService {
    private final ScenarioRepository scenarioRepository;
    private final ScenarioExecutionRepository scenarioExecutionRepository;
    private final TaskExecutionRepository taskExecutionRepository;
    private final WebClient.Builder webClientBuilder;

    @Async
    @Transactional
    public void runScenario(UUID scenarioId, Map<String, Object> payload) {
        var scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));

        var tasks = scenario.getTasks();
        if (tasks.isEmpty()) {
            throw new RuntimeException("Scenario has no tasks");
        }

        // Create scenario execution
        final ScenarioExecution scenarioExecution = ScenarioExecution.builder()
                .scenario(scenario)
                .state(ScenarioExecutionState.STARTED)
                .build();

        // Create task executions
        var executionTasks = tasks.stream().map(task -> TaskExecution.builder()
                .state(TaskExecutionState.PENDING)
                .actionUri(task.getAction().getPlugin().getUrl() + "/actions/" + task.getAction().getName())
                .scenarioExecution(scenarioExecution)
                .build()).toList();

        scenarioExecution.setTasks(executionTasks);
        scenarioExecutionRepository.save(scenarioExecution);

        // Execute first task
        executeTask(executionTasks.get(0), scenarioExecution, payload)
                .publishOn(Schedulers.boundedElastic())
                .doOnError(taskErrorHandler(executionTasks.get(0).getId(), scenarioExecution.getId()))
                .subscribe();
    }

    @Async
    @Transactional
    public void taskHandler(UUID taskExecutionId, UUID scenarioExecutionId, Map<String, Object> payload) {
        var scenarioExecution = scenarioExecutionRepository.findById(scenarioExecutionId)
                .orElseThrow(() -> new RuntimeException("Scenario execution not found"));

        var executionTask = taskExecutionRepository.findById(taskExecutionId)
                .orElseThrow(() -> new RuntimeException("Task execution not found"));

        var executionTasks = scenarioExecution.getTasks();

        // Find current task index
        var currentTaskIndex = executionTasks.indexOf(executionTask);
        if (currentTaskIndex == -1) {
            throw new RuntimeException("Task execution not found");
        }

        // Update task execution
        executionTask.setEndTime(LocalDateTime.now());
        executionTask.setState(TaskExecutionState.COMPLETED);

        // Check if task is last and complete scenario execution
        if (currentTaskIndex == executionTasks.size() - 1) {
            scenarioExecution.setState(ScenarioExecutionState.COMPLETED);
            scenarioExecution.setEndTime(LocalDateTime.now());
            scenarioExecutionRepository.save(scenarioExecution);
            return;
        }

        // Execute next task
        executeTask(executionTasks.get(currentTaskIndex + 1), scenarioExecution, payload)
                .publishOn(Schedulers.boundedElastic())
                .doOnError(taskErrorHandler(executionTask.getId(), scenarioExecution.getId()))
                .subscribe();
    }

    private Mono<Void> executeTask(TaskExecution taskExecution, ScenarioExecution scenarioExecution, Map<String, Object> payload) {
        taskExecution.setState(TaskExecutionState.STARTED);
        taskExecution.setStartTime(LocalDateTime.now());

        return webClientBuilder.build().post()
                .uri(taskExecution.getActionUri())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-ID", scenarioExecution.getScenario().getOwner().getId().toString())
                .header("X-Task-Execution-ID", taskExecution.getId().toString())
                .header("X-Scenario-Execution-ID", scenarioExecution.getId().toString())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class);
    }

    private Consumer<? super Throwable> taskErrorHandler(UUID taskExecutionId, UUID scenarioExecutionId) {
        return error -> {
            taskExecutionRepository.findById(taskExecutionId).ifPresent(taskExecution -> {
                taskExecution.setState(TaskExecutionState.FAILED);
                taskExecution.setEndTime(LocalDateTime.now());
                taskExecutionRepository.save(taskExecution);
            });

            scenarioExecutionRepository.findById(scenarioExecutionId).ifPresent(scenarioExecution -> {
                scenarioExecution.setState(ScenarioExecutionState.FAILED);
                scenarioExecution.setEndTime(LocalDateTime.now());
                scenarioExecutionRepository.save(scenarioExecution);
            });
        };
    }
}
