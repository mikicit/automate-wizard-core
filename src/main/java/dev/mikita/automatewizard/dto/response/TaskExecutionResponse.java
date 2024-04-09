package dev.mikita.automatewizard.dto.response;

import dev.mikita.automatewizard.entity.TaskExecutionState;
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
public class TaskExecutionResponse {
    private UUID id;
    private UUID scenarioExecutionId;
    private TaskExecutionState state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String preprocessor;
}
