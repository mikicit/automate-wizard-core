package dev.mikita.automatewizard.dto.response;

import dev.mikita.automatewizard.entity.ScenarioState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioStateResponse {
    private ScenarioState state;
}
