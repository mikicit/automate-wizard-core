package dev.mikita.automatewizard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionResponse {
    public String name;
    public String path;
    public String method;
    public UUID pluginId;
}