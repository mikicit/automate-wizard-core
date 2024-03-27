package dev.mikita.automatewizard.service;

import dev.mikita.automatewizard.entity.Action;
import dev.mikita.automatewizard.repository.ActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActionService {
    private final ActionRepository actionRepository;

    public List<Action> getActions() {
        return actionRepository.findAll();
    }

    public Action getAction(UUID id) {
        return actionRepository.findById(id).orElseThrow(() -> new RuntimeException("Action not found"));
    }

    public List<Action> getActionsByPluginId(UUID pluginId) {
        return actionRepository.findAllByPluginId(pluginId).orElseThrow(() -> new RuntimeException("Actions not found"));
    }
}
