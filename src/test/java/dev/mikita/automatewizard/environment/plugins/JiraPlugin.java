package dev.mikita.automatewizard.environment.plugins;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.UniformDistribution;
import java.io.IOException;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.wiremock.webhooks.Webhooks.webhook;

public class JiraPlugin extends RemotePlugin {
    public JiraPlugin(String coreUrl) throws IOException {
        super(coreUrl, "/plugins/jira.json");
    }

    @Override
    protected void initActionsStubs() {
        wm.stubFor(post(urlEqualTo("/actions/get-project-participants"))
                .withHeader("X-User-Id", matching(".+"))
                .withHeader("X-Task-Execution-Id", matching(".+"))
                .withRequestBody(matchingJsonPath("$.projectId"))
                .willReturn(ok())
                .withServeEventListener("webhook", webhook()
                        .withDelay(new UniformDistribution(500, 2000))
                        .withMethod(RequestMethod.POST)
                        .withUrl(coreUrl + "/api/v1/webhooks/tasks/" + "{{jsonPath originalRequest.headers '$.X-Task-Execution-Id'}}")
                        .withBody("""
                                {
                                    "state": "SUCCESS",
                                    "message": "",
                                    "result": {
                                        "participants": [{
                                            "userId": "d028e5ab-d157-477c-921c-1e21c5d444b6",
                                            "slackId": "f2e07b76-d230-4f72-82bc-a7282ddb2eb2"
                                        }]
                                    }
                                }
                                """)
                        .withHeader("Content-Type", "application/json")
                ));

        wm.stubFor(post(urlEqualTo("/actions/assign-task"))
                .withHeader("X-User-Id", matching(".+"))
                .withHeader("X-Task-Execution-Id", matching(".+"))
                .withRequestBody(matchingJsonPath("$.projectId")
                        .and(matchingJsonPath("$.taskId"))
                        .and(matchingJsonPath("$.userId"))
                )
                .willReturn(ok())
                .withServeEventListener("webhook", webhook()
                        .withDelay(new UniformDistribution(500, 2000))
                        .withMethod(RequestMethod.POST)
                        .withUrl(coreUrl + "/api/v1/webhooks/tasks/" + "{{jsonPath originalRequest.headers '$.X-Task-Execution-Id'}}")
                        .withBody("""
                                {
                                    "state": "SUCCESS",
                                    "message": ""
                                }
                                """)
                        .withHeader("Content-Type", "application/json")
                ));
    }

    @Override
    protected void initTriggersStubs() {
        wm.stubFor(post(urlEqualTo("/triggers/new-task-trigger"))
                .withHeader("X-User-Id", matching(".+"))
                .withHeader("X-Scenario-Id", matching(".+"))
                .withRequestBody(matchingJsonPath("$.projectId"))
                .willReturn(ok())
                .withServeEventListener("webhook", webhook()
                        .withFixedDelay(1000)
                        .withMethod(RequestMethod.POST)
                        .withUrl(coreUrl + "/api/v1/webhooks/triggers/" + "{{jsonPath originalRequest.headers '$.X-Scenario-Id'}}")
                        .withBody("""
                                {
                                    "taskId": "15cfe7fe-b739-4ce7-a777-9d9694816f79",
                                    "projectId": "858f6f7f-0225-4cf5-8c3a-b0aaa2d4b2ab"
                                }
                                """)
                        .withHeader("Content-Type", "application/json")
                ));

        wm.stubFor(delete(urlEqualTo("/triggers/new-task-trigger"))
                .withHeader("X-User-Id", matching(".+"))
                .withHeader("X-Scenario-Id", matching(".+"))
                .willReturn(aResponse()
                        .withStatus(204)
                ));
    }
}
