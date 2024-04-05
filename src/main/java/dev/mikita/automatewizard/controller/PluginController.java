package dev.mikita.automatewizard.controller;

import dev.mikita.automatewizard.dto.request.AddPluginRequest;
import dev.mikita.automatewizard.dto.response.ActionResponse;
import dev.mikita.automatewizard.dto.response.PluginResponse;
import dev.mikita.automatewizard.dto.response.TriggerResponse;
import dev.mikita.automatewizard.entity.User;
import dev.mikita.automatewizard.service.PluginService;
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
@RequestMapping(path = "/api/v1/plugins")
@RequiredArgsConstructor
public class PluginController {
    private final PluginService pluginService;

    @GetMapping
    public ResponseEntity<List<PluginResponse>> getPlugins() {
        return ResponseEntity.ok(new ModelMapper().map(
                pluginService.getAllPlugins(),
                new ParameterizedTypeReference<List<PluginResponse>>() {}.getType()));
    }

    @GetMapping("/installed")
    public ResponseEntity<List<PluginResponse>> getInstalledPlugins(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(
                pluginService.getInstalledPlugins(user),
                new ParameterizedTypeReference<List<PluginResponse>>() {}.getType()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<PluginResponse>> getMyPlugins(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new ModelMapper().map(
                pluginService.getUserPlugins(user),
                new ParameterizedTypeReference<List<PluginResponse>>() {}.getType()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PluginResponse> getPlugin(@PathVariable UUID id) {
        return ResponseEntity.ok(new ModelMapper().map(pluginService.getPlugin(id), PluginResponse.class));
    }

    @PostMapping
    public ResponseEntity<PluginResponse> createPlugin(@RequestBody AddPluginRequest request,
                                                       @AuthenticationPrincipal User user) {
        var plugin = pluginService.createPlugin(request, user);

        // Create resource URI
        String resourceUri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(plugin.getId())
                .toUriString();

        return ResponseEntity.created(URI.create(resourceUri))
                .body(new ModelMapper().map(plugin, PluginResponse.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlugin(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        pluginService.deletePlugin(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/actions")
    public ResponseEntity<List<ActionResponse>> getActions(@PathVariable UUID id) {
        return ResponseEntity.ok(new ModelMapper().map(
                pluginService.getActionsByPluginId(id),
                new ParameterizedTypeReference<List<ActionResponse>>() {}.getType()));
    }

    @GetMapping("/{id}/triggers")
    public ResponseEntity<List<TriggerResponse>> getTriggers(@PathVariable UUID id) {
        return ResponseEntity.ok(new ModelMapper().map(
                pluginService.getTriggersByPluginId(id),
                new ParameterizedTypeReference<List<TriggerResponse>>() {}.getType()));
    }

    @PostMapping("/{id}/install")
    public ResponseEntity<Void> installPlugin(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        pluginService.installPlugin(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/uninstall")
    public ResponseEntity<Void> uninstallPlugin(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        pluginService.uninstallPlugin(id, user);
        return ResponseEntity.noContent().build();
    }
}
