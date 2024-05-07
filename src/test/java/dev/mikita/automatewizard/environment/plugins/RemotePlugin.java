package dev.mikita.automatewizard.environment.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.util.Objects;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public abstract class RemotePlugin {
    protected final WireMockServer wm;

    @Getter
    protected final JsonNode definition;
    protected final String coreUrl;

    public RemotePlugin(String coreUrl, String definitionPath) throws IOException {
        Objects.requireNonNull(coreUrl);
        this.coreUrl = coreUrl;
        wm = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        definition = new ObjectMapper().readTree(new ClassPathResource(definitionPath).getFile());
        initStubs();
    }

    private void initLifecycleStubs() {
        wm.stubFor(get(urlEqualTo("/"))
                .withHeader("X-User-Id", matching(".+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                ));

        wm.stubFor(post(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(definition.toString())
                ));

        wm.stubFor(delete(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(204)
                ));

        wm.stubFor(post(urlEqualTo("/install"))
                .withHeader("X-User-Id", matching(".+"))
                .willReturn(aResponse()
                        .withStatus(200)
                ));

        wm.stubFor(post(urlEqualTo("/uninstall"))
                .withHeader("X-User-Id", matching(".+"))
                .willReturn(aResponse()
                        .withStatus(200)
                ));
    }

    private void initStubs() {
        initLifecycleStubs();
        initActionsStubs();
        initTriggersStubs();
    }

    protected void initActionsStubs() {}
    protected void initTriggersStubs() {}

    public void start() {
        wm.start();
    }

    public void stop() {
        wm.stop();
    }

    public String getBaseUrl() {
        return wm.baseUrl();
    }
}
