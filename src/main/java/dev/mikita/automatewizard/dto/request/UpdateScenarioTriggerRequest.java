package dev.mikita.automatewizard.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScenarioTriggerRequest {
    private UUID triggerId;
    private JsonNode triggerPayload;
}
