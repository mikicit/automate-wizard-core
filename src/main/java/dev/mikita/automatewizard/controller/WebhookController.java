package dev.mikita.automatewizard.controller;

import dev.mikita.automatewizard.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final ScenarioService scenarioService;

    @PostMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<Void> handleWebhook(@PathVariable UUID id,
                                              @RequestBody(required = false) Map<String, Object> payload) {
        if (payload == null) {
            payload = new HashMap<>();
        }

        scenarioService.webhookHandler(id, payload);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(path = "/triggers/{id}", consumes = "application/json")
    public ResponseEntity<Void> handleTrigger(@PathVariable UUID id,
                                              @RequestBody(required = false) Map<String, Object> payload) {
        if (payload == null) {
            payload = new HashMap<>();
        }

        scenarioService.triggerHandler(id, payload);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(path = "/tasks/{id}", consumes = "application/json")
    public ResponseEntity<Void> handleTask(@PathVariable UUID id,
                                           @RequestBody(required = false) Map<String, Object> payload) {
        if (payload == null) {
            payload = new HashMap<>();
        }

        scenarioService.taskHandler(id, payload);
        return ResponseEntity.accepted().build();
    }
}
