package dev.mikita.automatewizard.environment.plugins;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.UniformDistribution;
import java.io.IOException;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.wiremock.webhooks.Webhooks.webhook;

public class SlackPlugin extends RemotePlugin {
    public SlackPlugin(String coreUrl) throws IOException {
        super(coreUrl, "/plugins/slack.json");
    }

    @Override
    protected void initActionsStubs() {
        wm.stubFor(post(urlEqualTo("/actions/send-private-message"))
                .withHeader("X-User-Id", matching(".+"))
                .withHeader("X-Task-Execution-Id", matching(".+"))
                .withRequestBody(matchingJsonPath("$.userId")
                        .and(matchingJsonPath("$.message"))
                )
                .willReturn(ok())
                .withServeEventListener("webhook", webhook()
                        .withDelay(new UniformDistribution(500, 2000))
                        .withMethod(RequestMethod.POST)
                        .withUrl(coreUrl + "/api/v1/webhooks/tasks/" + "{{jsonPath originalRequest.headers '$.X-Task-Execution-Id'}}")
                        .withBody("{}")
                        .withHeader("Content-Type", "application/json")
                ));
    }
}
