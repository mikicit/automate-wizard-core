package dev.mikita.automatewizard.controller;

import dev.mikita.automatewizard.dto.request.CreateScenarioRequest;
import dev.mikita.automatewizard.dto.response.ScenarioResponse;
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

    @GetMapping
    public ResponseEntity<List<ScenarioResponse>> getScenarios(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(scenarioService.getScenarios(user),
                new ParameterizedTypeReference<List<ScenarioResponse>>() {
        }.getType()));
    }

    @GetMapping(path = "/{id}")
    private ResponseEntity<ScenarioResponse> getScenario(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(scenarioService.getScenario(id, user), ScenarioResponse.class));
    }

    @PostMapping
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
}
