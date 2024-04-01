package dev.mikita.automatewizard.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo {
    private String message;
    private List<String> errors;
    private String requestUri;

    @Override
    public String toString() {
        return "ErrorInfo{" +
                "message='" + message + '\'' +
                ", errors=" + errors +
                ", requestUri='" + requestUri + '\'' +
                '}';
    }
}
