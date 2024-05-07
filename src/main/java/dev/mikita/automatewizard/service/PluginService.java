package dev.mikita.automatewizard.service;

import dev.mikita.automatewizard.dto.request.AddPluginRequest;
import dev.mikita.automatewizard.dto.response.AddPluginResponse;
import dev.mikita.automatewizard.entity.*;
import dev.mikita.automatewizard.exception.IllegalStateException;
import dev.mikita.automatewizard.exception.NotFoundException;
import dev.mikita.automatewizard.repository.ActionRepository;
import dev.mikita.automatewizard.repository.InstalledPluginRepository;
import dev.mikita.automatewizard.repository.PluginRepository;
import dev.mikita.automatewizard.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PluginService {
    private final PluginRepository pluginRepository;
    private final InstalledPluginRepository installedPluginRepository;
    private final ActionRepository actionRepository;
    private final TriggerRepository triggerRepository;
    private final WebClient.Builder webClientBuilder;

    public List<Plugin> getAllPlugins() {
        return pluginRepository.findAll();
    }

    public Plugin getPlugin(UUID id) {
        return pluginRepository.findById(id).orElseThrow(() -> new RuntimeException("Plugin not found"));
    }

    public Plugin createPlugin(AddPluginRequest request, User user) {
        // Send plugin info request to plugin server
        var response = webClientBuilder.baseUrl(request.getUrl()).build().post()
                .uri("/")
                .retrieve()
                .bodyToMono(AddPluginResponse.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Failed to fetch plugin info");
        }

        var plugin = Plugin.builder()
                .name(response.getName())
                .author(user)
                .description(response.getDescription())
                .url(request.getUrl())
                .build();

        // Map actions from response to Action entities
        var actions = response.getActions().stream().map(action -> {
                    var mappedAction = new ModelMapper().map(action, Action.class);
                    mappedAction.setPlugin(plugin);
                    return mappedAction;
                }).toList();

        plugin.setActions(actions);

        // Map triggers from response to Trigger entities
        var triggers = response.getTriggers().stream().map(trigger -> {
                    var mappedTrigger = new ModelMapper().map(trigger, Trigger.class);
                    mappedTrigger.setPlugin(plugin);
                    return mappedTrigger;
                }).toList();

        plugin.setTriggers(triggers);

        return pluginRepository.save(plugin);
    }

    public void deletePlugin(UUID id, User user) {
        var plugin = pluginRepository.findById(id).orElseThrow();
        if (!plugin.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("You are not the author of this plugin");
        }

        // Send delete request to plugin server
        ClientResponse response = webClientBuilder.baseUrl(plugin.getUrl()).build().delete()
                .uri("/")
                .exchange()
                .block();

        if (response == null || response.statusCode().isError()) {
            throw new RuntimeException("Failed to delete plugin");
        }

        pluginRepository.delete(plugin);
    }

    public List<Plugin> getInstalledPlugins(User user) {
        return installedPluginRepository.findAllByUserId(user.getId())
                .map(plugins -> plugins.stream().map(InstalledPlugin::getPlugin).toList()).orElseGet(List::of);
    }

    public List<Plugin> getUserPlugins(User user) {
        return pluginRepository.findAllByAuthorId(user.getId()).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public void installPlugin(UUID id, User user) {
        var plugin = pluginRepository.findById(id).orElseThrow(() -> new NotFoundException("Plugin not found"));

        var installedPlugin = InstalledPlugin.builder()
                .id(new InstalledPluginId(user.getId(), id))
                .plugin(plugin)
                .user(user)
                .build();

        // Send install request to plugin server
        ClientResponse response = webClientBuilder.baseUrl(plugin.getUrl()).build().post()
                .uri("/install")
                .header("X-User-ID", user.getId().toString())
                .exchange()
                .block();

        if (response == null || response.statusCode().isError()) {
            throw new IllegalStateException("Failed to install plugin");
        }

        installedPluginRepository.save(installedPlugin);
    }

    public void uninstallPlugin(UUID id, User user) {
        var installedPlugin = installedPluginRepository.findByPluginIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Plugin not found"));

        // Send uninstall request to plugin server
        ClientResponse response = webClientBuilder.baseUrl(installedPlugin.getPlugin().getUrl()).build().post()
                .uri("/uninstall")
                .header("X-User-ID", user.getId().toString())
                .exchange()
                .block();

        if (response == null || response.statusCode().isError()) {
            throw new IllegalStateException("Failed to install plugin");
        }

        installedPluginRepository.delete(installedPlugin);
    }

    public List<Action> getActionsByPluginId(UUID pluginId) {
        return actionRepository.findAllByPluginId(pluginId).orElseThrow(() -> new NotFoundException("Actions not found"));
    }

    public List<Trigger> getTriggersByPluginId(UUID pluginId) {
        return triggerRepository.findAllByPluginId(pluginId).orElseThrow(() -> new NotFoundException("Triggers not found"));
    }
}
