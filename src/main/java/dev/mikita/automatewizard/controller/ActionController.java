package dev.mikita.automatewizard.controller;

import dev.mikita.automatewizard.dto.response.ActionResponse;
import dev.mikita.automatewizard.service.ActionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/actions")
@RequiredArgsConstructor
public class ActionController {
    private final ActionService actionService;

    @GetMapping
    public ResponseEntity<List<ActionResponse>> getActions() {
        return ResponseEntity.ok(new ModelMapper().map(actionService.getActions(),
                new ParameterizedTypeReference<List<ActionResponse>>() {}.getType()));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ActionResponse> getAction(@PathVariable UUID id) {
        return ResponseEntity.ok(new ModelMapper().map(actionService.getAction(id), ActionResponse.class));
    }
}
