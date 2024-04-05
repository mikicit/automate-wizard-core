package dev.mikita.automatewizard.controller;

import dev.mikita.automatewizard.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/webhooks")
@RequiredArgsConstructor
public class WebHookController {
    private final ScenarioService scenarioService;

    @PostMapping(path = "/triggers/{id}", consumes = "application/json")
    public ResponseEntity<Void> handleFiredTrigger(@PathVariable UUID id,
                                                   @RequestHeader("X-Scenario-ID") UUID scenarioId,
                                                   @RequestBody(required = false) Map<String, Object> payload) {
        if (payload == null) {
            payload = Map.of();
        }

        scenarioService.triggerHandler(id, scenarioId, payload);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(path = "/tasks/{id}", consumes = "application/json")
    public ResponseEntity<Void> handleCompletedTask(@PathVariable UUID id,
                                                    @RequestHeader("X-Scenario-Execution-ID") UUID scenarioExecutionId,
                                                    @RequestBody(required = false) Map<String, Object> payload) {
        if (payload == null) {
            payload = Map.of();
        }

        scenarioService.taskHandler(id, scenarioExecutionId, payload);
        return ResponseEntity.accepted().build();
    }
}
