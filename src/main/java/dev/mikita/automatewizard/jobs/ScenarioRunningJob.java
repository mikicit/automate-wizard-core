package dev.mikita.automatewizard.jobs;

import dev.mikita.automatewizard.service.ScenarioExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import reactor.util.annotation.NonNull;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRunningJob extends QuartzJobBean {
    private final ScenarioExecutionService scenarioExecutionService;

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {
        scenarioExecutionService.runScenario(
                UUID.fromString(context.getJobDetail().getJobDataMap().get("scenarioId").toString()), new HashMap<>());
    }
}
