package dev.mikita.automatewizard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookTriggerRequest {
    private UUID userId;
    private UUID triggerId;
    private Map<String, Object> payload;
}
