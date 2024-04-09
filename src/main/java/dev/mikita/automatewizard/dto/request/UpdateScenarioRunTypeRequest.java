package dev.mikita.automatewizard.dto.request;

import dev.mikita.automatewizard.entity.ScenarioRunType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScenarioRunTypeRequest {
    private ScenarioRunType runType;
}
