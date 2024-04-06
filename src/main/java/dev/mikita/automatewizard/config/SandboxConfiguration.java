package dev.mikita.automatewizard.config;

import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.NashornSandboxes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.Executors;

@Configuration
public class SandboxConfiguration {
    @Bean
    public NashornSandbox nashornSandbox() {
        NashornSandbox sandbox = NashornSandboxes.create();
        sandbox.setMaxCPUTime(100);
        sandbox.setMaxMemory(50*1024);
        sandbox.allowNoBraces(false);
        sandbox.setMaxPreparedStatements(30);
        sandbox.setExecutor(Executors.newSingleThreadExecutor());

        return sandbox;
    }
}
