package dev.mikita.automatewizard.service;

import dev.mikita.automatewizard.entity.Trigger;
import dev.mikita.automatewizard.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TriggerService {
    private final TriggerRepository triggerRepository;

    public List<Trigger> getTriggers() {
        return triggerRepository.findAll();
    }

    public Trigger getTrigger(UUID id) {
        return triggerRepository.findById(id).orElseThrow(() -> new RuntimeException("Trigger not found"));
    }

    public List<Trigger> getTriggersByPluginId(UUID pluginId) {
        return triggerRepository.findAllByPluginId(pluginId).orElseThrow(() -> new RuntimeException("Triggers not found"));
    }
}
