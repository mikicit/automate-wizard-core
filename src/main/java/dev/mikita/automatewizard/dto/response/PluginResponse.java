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
public class PluginResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID authorId;
    private String url;
}