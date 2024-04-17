package dev.mikita.automatewizard.jobs;

import dev.mikita.automatewizard.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import reactor.util.annotation.NonNull;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRunningJob extends QuartzJobBean {
    private final ScenarioService scenarioService;

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {
        scenarioService.runScenarioBySchedule(
                UUID.fromString(context.getJobDetail().getJobDataMap().get("scenarioId").toString()));
    }
}
