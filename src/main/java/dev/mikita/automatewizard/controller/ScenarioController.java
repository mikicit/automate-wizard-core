package dev.mikita.automatewizard.controller;

import dev.mikita.automatewizard.dto.request.*;
import dev.mikita.automatewizard.dto.response.*;
import dev.mikita.automatewizard.entity.User;
import dev.mikita.automatewizard.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/scenarios")
@RequiredArgsConstructor
public class ScenarioController {
    private final ScenarioService scenarioService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<ScenarioResponse>> getScenarios(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(scenarioService.getScenarios(user),
                new ParameterizedTypeReference<List<ScenarioResponse>>() {
        }.getType()));
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    private ResponseEntity<ScenarioResponse> getScenario(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(scenarioService.getScenario(id, user), ScenarioResponse.class));
    }

    @GetMapping(path = "/{id}/executions", produces = "application/json")
    private ResponseEntity<List<ScenarioExecutionResponse>> getExecutions(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(scenarioService.getExecutions(id, user),
                new ParameterizedTypeReference<List<ScenarioExecutionResponse>>() {
        }.getType()));
    }

    @GetMapping(path = "/{id}/executions/{executionId}", produces = "application/json")
    private ResponseEntity<ScenarioExecutionResponse> getExecution(@PathVariable UUID id, @PathVariable UUID executionId,
                                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(scenarioService.getExecution(id, executionId, user),
                ScenarioExecutionResponse.class));
    }

    @GetMapping(path = "/{id}/executions/{executionId}/tasks", produces = "application/json")
    private ResponseEntity<List<TaskExecutionResponse>> getExecutionTasks(@PathVariable UUID id, @PathVariable UUID executionId,
                                                                          @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(scenarioService.getTaskExecutions(id, executionId, user),
                new ParameterizedTypeReference<List<TaskExecutionResponse>>() {
        }.getType()));
    }

    @GetMapping(path = "/{id}/executions/{executionId}/tasks/{taskId}", produces = "application/json")
    private ResponseEntity<TaskExecutionResponse> getExecutionTask(@PathVariable UUID id, @PathVariable UUID executionId,
                                                                   @PathVariable UUID taskId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(scenarioService.getTaskExecution(id, executionId, taskId, user),
                TaskExecutionResponse.class));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    private ResponseEntity<ScenarioResponse> createScenario(@RequestBody CreateScenarioRequest request,
                                             @AuthenticationPrincipal User user) {
        var scenario = scenarioService.createScenario(request, user);

        String resourceUri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(scenario.getId())
                .toUriString();

        return ResponseEntity.created(URI.create(resourceUri))
                .body(new ModelMapper().map(scenario, ScenarioResponse.class));
    }

    @DeleteMapping(path = "/{id}")
    private ResponseEntity<Void> deleteScenario(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        scenarioService.deleteScenario(id, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/{id}/state", consumes = "application/json", produces = "application/json")
    private ResponseEntity<ScenarioStateResponse> updateState(
            @PathVariable UUID id, @RequestBody UpdateScenarioStateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ScenarioStateResponse.builder()
                .state(scenarioService.updateState(id, request.getState(), user)).build());
    }

    @PutMapping(path = "/{id}/run", consumes = "application/json", produces = "application/json")
    private ResponseEntity<ScenarioRunTypeResponse> updateRunType(
            @PathVariable UUID id, @RequestBody UpdateScenarioRunTypeRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ScenarioRunTypeResponse.builder()
                .runType(scenarioService.updateRunType(id, request.getRunType(), user)).build());
    }

    @PutMapping(path = "/{id}/trigger", consumes = "application/json", produces = "application/json")
    private ResponseEntity<ScenarioTriggerResponse> updateTrigger(
            @PathVariable UUID id, @RequestBody UpdateScenarioTriggerRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ScenarioTriggerResponse.builder()
                .triggerId(scenarioService.updateTrigger(id, request.getTriggerId(), user).getId()).build());
    }

    @PostMapping(path = "/{id}/run")
    private ResponseEntity<Void> runScenario(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        scenarioService.runScenarioManual(id, user);
        return ResponseEntity.accepted().build();
    }

    @GetMapping(path = "/{id}/tasks", produces = "application/json")
    private ResponseEntity<List<TaskResponse>> getTasks(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(scenarioService.getTasks(id, user),
                new ParameterizedTypeReference<List<TaskResponse>>() {
        }.getType()));
    }

    @PutMapping(path = "/{id}/tasks", consumes = "application/json", produces = "application/json")
    private ResponseEntity<List<TaskResponse>> updateTasks(@PathVariable UUID id, @RequestBody List<TaskRequest> tasks,
                                                           @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(scenarioService.updateTasks(id, tasks, user),
                new ParameterizedTypeReference<List<TaskResponse>>() {
        }.getType()));
    }
}
