package dev.mikita.automatewizard.controller;

import dev.mikita.automatewizard.dto.response.TriggerResponse;
import dev.mikita.automatewizard.service.TriggerService;
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
@RequestMapping(path = "/api/v1/triggers")
@RequiredArgsConstructor
public class TriggerController {
    private final TriggerService triggerService;

    @GetMapping
    public ResponseEntity<List<TriggerResponse>> getTriggers() {
        return ResponseEntity.ok(new ModelMapper().map(triggerService.getTriggers(),
                new ParameterizedTypeReference<List<TriggerResponse>>() {}.getType()));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<TriggerResponse> getTrigger(@PathVariable UUID id) {
        return ResponseEntity.ok(new ModelMapper().map(triggerService.getTrigger(id), TriggerResponse.class));
    }
}