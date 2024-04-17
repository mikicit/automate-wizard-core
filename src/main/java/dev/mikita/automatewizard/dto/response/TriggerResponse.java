package dev.mikita.automatewizard.dto.response;

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
public class TriggerResponse {
    private UUID id;
    private String name;
    private String label;
    private String description;
    private UUID pluginId;
    private JsonNode consumes;
    private JsonNode produces;
}
