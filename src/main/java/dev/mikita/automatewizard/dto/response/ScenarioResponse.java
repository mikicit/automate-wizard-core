package dev.mikita.automatewizard.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import dev.mikita.automatewizard.entity.ScenarioRunType;
import dev.mikita.automatewizard.entity.ScenarioState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioResponse {
    private UUID id;
    private String name;
    private ScenarioState state;
    private ScenarioRunType runType;
    private UUID triggerId;
    private JsonNode triggerPayload;
    private UUID webhookId;
    private String schedule;
}
