package dev.mikita.automatewizard.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginTaskExecutionRequest {
    public enum PluginTaskExecutionState {
        SUCCESS,
        FAILED
    }

    private PluginTaskExecutionState state;
    private String message;
    private JsonNode result;
}
