package dev.mikita.automatewizard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPluginResponse {
    private String name;
    private String description;
    private List<ActionResponse> actions;
    private List<TriggerResponse> triggers;
}
