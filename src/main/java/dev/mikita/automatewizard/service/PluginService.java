package dev.mikita.automatewizard.service;

import dev.mikita.automatewizard.dto.request.AddPluginRequest;
import dev.mikita.automatewizard.dto.response.AddPluginResponse;
import dev.mikita.automatewizard.entity.Action;
import dev.mikita.automatewizard.entity.InstalledPlugin;
import dev.mikita.automatewizard.entity.Plugin;
import dev.mikita.automatewizard.entity.User;
import dev.mikita.automatewizard.repository.InstalledPluginRepository;
import dev.mikita.automatewizard.repository.PluginRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PluginService {
    private final PluginRepository pluginRepository;
    private final InstalledPluginRepository installedPluginRepository;
    private final WebClient.Builder webClientBuilder;

    public List<Plugin> getAllPlugins() {
        return pluginRepository.findAll();
    }

    public Plugin getPlugin(UUID id) {
        return pluginRepository.findById(id).orElseThrow(() -> new RuntimeException("Plugin not found"));
    }

    public Plugin createPlugin(AddPluginRequest request, User user) {
        WebClient client = webClientBuilder.baseUrl(request.getUrl()).build();

        var response = client.get()
                .uri("/init")
                .retrieve()
                .bodyToMono(AddPluginResponse.class)
                .block();

        var plugin = Plugin.builder()
                .name(response.getName())
                .author(user)
                .description(response.getDescription())
                .url(request.getUrl())
                .build();

        var actions = response.getActions().stream().map(action -> {
                    var mappedAction = new ModelMapper().map(action, Action.class);
                    mappedAction.setPlugin(plugin);
                    return mappedAction;
                }).toList();

        plugin.setActions(actions);

        return pluginRepository.save(plugin);
    }

    public void deletePlugin(UUID id, User user) {
        var plugin = pluginRepository.findById(id.toString()).orElseThrow();
        if (!plugin.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("You are not the author of this plugin");
        }

        pluginRepository.deleteById(id.toString());
    }

    public InstalledPlugin installPlugin(UUID id, User user) {
        var plugin = pluginRepository.findById(id).orElseThrow(() -> new RuntimeException("Plugin not found"));

        var installedPlugin = InstalledPlugin.builder()
                .plugin(plugin)
                .user(user)
                .build();

        return installedPluginRepository.save(installedPlugin);
    }

    public void uninstallPlugin(UUID id, User user) {
        var plugin = installedPluginRepository.findByPluginIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Plugin not found"));

        installedPluginRepository.delete(plugin);
    }
}
