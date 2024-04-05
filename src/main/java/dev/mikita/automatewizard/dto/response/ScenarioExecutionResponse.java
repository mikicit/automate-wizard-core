package dev.mikita.automatewizard.dto.response;

import dev.mikita.automatewizard.entity.ScenarioExecutionState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioExecutionResponse {
    private UUID id;
    private UUID scenarioId;
    private ScenarioExecutionState state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
