package dev.mikita.automatewizard.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import dev.mikita.automatewizard.dto.request.PluginTaskExecutionRequest;
import dev.mikita.automatewizard.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final ScenarioService scenarioService;

    @PostMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<Void> handleWebhook(@PathVariable UUID id,
                                              @RequestBody(required = false) JsonNode payload) {
        if (payload == null) {
            payload = JsonNodeFactory.instance.objectNode();
        }

        scenarioService.runScenarioByWebhook(id, payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/triggers/{id}", consumes = "application/json")
    public ResponseEntity<Void> handleTrigger(@PathVariable UUID id,
                                              @RequestBody(required = false) JsonNode payload) {
        if (payload == null) {
            payload = JsonNodeFactory.instance.objectNode();
        }

        scenarioService.runScenarioByTrigger(id, payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/tasks/{id}", consumes = "application/json")
    public ResponseEntity<Void> handleTask(@PathVariable UUID id,
                                           @RequestBody PluginTaskExecutionRequest request) {
        if (request.getResult() == null) {
            request.setResult(JsonNodeFactory.instance.objectNode());
        }

        scenarioService.processTaskExecution(id, request);
        return ResponseEntity.ok().build();
    }
}
